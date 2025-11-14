import json
import re
from pathlib import Path
import shutil

BASE_ENTITY_PREFIX = "com.lycanitesmobs.core.entity.creature."

def repackage_entity(java_root: Path, json_path: Path, entity_class: str, creature_type: str):
    if not entity_class.startswith(BASE_ENTITY_PREFIX):
        print(f"Skipping {json_path.name}: entityClass not under core.entity.creature")
        return None

    parts = entity_class.split(".")
    class_name = parts[-1]
    pkg_parts = parts[:-1]

    try:
        creature_index = pkg_parts.index("creature")
    except ValueError:
        print(f"Skipping {json_path.name}: no 'creature' segment in entityClass")
        return None

    before_creature = pkg_parts[:creature_index + 1]
    after_creature = pkg_parts[creature_index + 1:]

    if after_creature and after_creature[0] == creature_type:
        print(f"Skipping {json_path.name}: already in '{creature_type}' subpackage")
        return None

    new_pkg_parts = before_creature + [creature_type]
    old_package = ".".join(pkg_parts)
    new_package = ".".join(new_pkg_parts)

    old_rel = Path(*pkg_parts) / f"{class_name}.java"
    new_rel_dir = Path(*new_pkg_parts)
    new_rel = new_rel_dir / f"{class_name}.java"

    old_file = java_root / old_rel
    new_file = java_root / new_rel

    if not old_file.is_file():
        print(f"Warning: Java file not found for {entity_class} at {old_file}")
        return None

    new_file.parent.mkdir(parents=True, exist_ok=True)
    content = old_file.read_text(encoding="utf-8")

    pattern = r"\bpackage\s+([^;]+);"
    match = re.search(pattern, content)
    if not match:
        print(f"Warning: no package declaration in {old_file}")
        return None

    current_pkg = match.group(1).strip()
    if current_pkg != old_package:
        print(f"Warning: package in file '{current_pkg}' != entityClass package '{old_package}' for {old_file}")
        old_package = current_pkg
        pkg_parts = old_package.split(".")
        try:
            creature_index = pkg_parts.index("creature")
        except ValueError:
            print(f"Skipping {json_path.name}: cannot align package path for {old_file}")
            return None
        before_creature = pkg_parts[:creature_index + 1]
        new_pkg_parts = before_creature + [creature_type]
        new_package = ".".join(new_pkg_parts)
        old_rel = Path(*pkg_parts) / f"{class_name}.java"
        new_rel_dir = Path(*new_pkg_parts)
        new_rel = new_rel_dir / f"{class_name}.java"
        old_file = java_root / old_rel
        new_file = java_root / new_rel
        if not old_file.is_file():
            print(f"Warning: realigned Java file not found for {current_pkg}.{class_name} at {old_file}")
            return None
        new_file.parent.mkdir(parents=True, exist_ok=True)
        content = old_file.read_text(encoding="utf-8")

    new_content = re.sub(pattern, f"package {new_package};", content, count=1)
    temp_target = new_file.with_suffix(".java.tmp")
    temp_target.write_text(new_content, encoding="utf-8")
    shutil.move(str(temp_target), str(new_file))
    old_file.unlink(missing_ok=True)

    new_entity_class = f"{new_package}.{class_name}"
    return new_entity_class

def update_usages(java_root: Path, moved_map: dict):
    if not moved_map:
        print("No moved entities, skipping usage update.")
        return

    simple_to_new = {}
    for old, new in moved_map.items():
        simple = new.split(".")[-1]
        simple_to_new.setdefault(simple, set()).add(new)

    java_files = list(java_root.rglob("*.java"))
    print(f"Updating usages in {len(java_files)} Java files...")
    changed_files = 0

    for path in java_files:
        text = path.read_text(encoding="utf-8")
        original = text

        pkg_match = re.search(r"^\s*package\s+([^\s;]+);", text, flags=re.MULTILINE)
        current_pkg = pkg_match.group(1).strip() if pkg_match else ""

        imports = re.findall(r"^\s*import\s+([^\s;]+);", text, flags=re.MULTILINE)
        import_set = set(imports)

        for old, new in moved_map.items():
            if old in text:
                text = text.replace(f"import {old};", f"import {new};")
                text = text.replace(old, new)

        for simple, new_set in simple_to_new.items():
            if len(new_set) != 1:
                continue
            new_fqcn = list(new_set)[0]
            new_pkg = ".".join(new_fqcn.split(".")[:-1])
            if current_pkg == new_pkg:
                continue
            if new_fqcn in import_set:
                continue
            pattern_existing_import = rf"^\s*import\s+[^\s;]*\.{simple};"
            if re.search(pattern_existing_import, text, flags=re.MULTILINE):
                continue
            if not re.search(rf"\b{simple}\b", text):
                continue

            lines = text.splitlines()
            insert_idx = -1
            last_import_idx = -1
            package_idx = -1

            for i, line in enumerate(lines):
                if re.match(r"^\s*package\s+", line):
                    package_idx = i
                elif re.match(r"^\s*import\s+", line):
                    last_import_idx = i

            import_line = f"import {new_fqcn};"
            if last_import_idx >= 0:
                insert_idx = last_import_idx + 1
            elif package_idx >= 0:
                insert_idx = package_idx + 1
            else:
                insert_idx = 0

            lines.insert(insert_idx, import_line)
            text = "\n".join(lines)
            import_set.add(new_fqcn)

        if text != original:
            path.write_text(text, encoding="utf-8")
            changed_files += 1
            print(f"Updated references in {path}")

    print(f"Finished updating usages. {changed_files} files changed.")

def main():
    main_dir = Path(__file__).parent
    java_root = main_dir / "java"
    resources_root = main_dir / "resources"
    creatures_dir = resources_root / "common" / "lycanitesmobs" / "creatures"

    if not java_root.is_dir():
        print(f"Java root not found: {java_root}")
        return
    if not creatures_dir.is_dir():
        print(f"Creatures directory not found: {creatures_dir}")
        return

    total = 0
    moved = 0
    moved_map = {}

    for json_path in sorted(creatures_dir.glob("*.json")):
        total += 1
        try:
            data = json.loads(json_path.read_text(encoding="utf-8"))
        except Exception as e:
            print(f"Error reading JSON {json_path}: {e}")
            continue

        creature_type = data.get("creatureType")
        entity_class = data.get("entityClass")

        if not creature_type or not entity_class:
            print(f"Skipping {json_path.name}: missing creatureType or entityClass")
            continue

        new_entity_class = repackage_entity(java_root, json_path, entity_class, creature_type)
        if not new_entity_class:
            continue

        data["entityClass"] = new_entity_class
        json_path.write_text(json.dumps(data, indent=4, ensure_ascii=False) + "\n", encoding="utf-8")
        moved += 1
        moved_map[entity_class] = new_entity_class
        print(f"Repackaged {entity_class} -> {new_entity_class} from {json_path.name}")

    print(f"Repackaged {moved}/{total} JSON entries.")
    update_usages(java_root, moved_map)

if __name__ == "__main__":
    main()
