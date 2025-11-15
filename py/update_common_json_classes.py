import json
from pathlib import Path


def find_classes(base_dir: Path, base_package: str):
    mapping = {}
    if not base_dir.is_dir():
        return mapping
    for path in base_dir.rglob("*.java"):
        rel = path.relative_to(base_dir)
        parts = list(rel.parts)
        class_name = path.stem
        pkg_suffix_parts = parts[:-1]
        if pkg_suffix_parts:
            pkg_suffix = ".".join(pkg_suffix_parts)
            fqcn = base_package + "." + pkg_suffix + "." + class_name
        else:
            fqcn = base_package + "." + class_name
        if class_name in mapping and mapping[class_name] != fqcn:
            print(f"Warning: duplicate class name {class_name} for {mapping[class_name]} and {fqcn}")
        mapping[class_name] = fqcn
    return mapping


def update_json_node(node, model_map, entity_map):
    changed = False

    if isinstance(node, dict):
        for key, value in list(node.items()):
            if key == "modelClass" and isinstance(value, str):
                old = value
                simple = old.split(".")[-1]
                new = model_map.get(simple)
                if new and new != old:
                    node[key] = new
                    changed = True
            elif key == "entityClass" and isinstance(value, str):
                old = value
                simple = old.split(".")[-1]
                new = entity_map.get(simple)
                if new and new != old:
                    node[key] = new
                    changed = True
            else:
                if isinstance(value, (dict, list)):
                    if update_json_node(value, model_map, entity_map):
                        changed = True

    elif isinstance(node, list):
        for item in node:
            if isinstance(item, (dict, list)):
                if update_json_node(item, model_map, entity_map):
                    changed = True

    return changed


def update_json_file(path: Path, model_map, entity_map):
    with path.open("r", encoding="utf-8") as f:
        data = json.load(f)

    changed = update_json_node(data, model_map, entity_map)

    if changed:
        with path.open("w", encoding="utf-8") as f:
            json.dump(data, f, indent=4, ensure_ascii=False)
            f.write("\n")
    return changed


def process_folder(json_dir: Path, model_map, entity_map, label: str):
    if not json_dir.is_dir():
        print(f"{label} directory not found: {json_dir}")
        return 0, 0
    updated_count = 0
    total_files = 0
    for json_path in sorted(json_dir.glob("*.json")):
        total_files += 1
        if update_json_file(json_path, model_map, entity_map):
            updated_count += 1
            print(f"[{label}] Updated: {json_path}")
        else:
            print(f"[{label}] No changes: {json_path}")
    print(f"[{label}] Done. {updated_count}/{total_files} JSON files updated.")
    return updated_count, total_files


def main():
    main_dir = Path(__file__).parent
    java_root = main_dir / "java"
    resources_root = main_dir / "resources"

    creatures_dir = resources_root / "common" / "lycanitesmobs" / "creatures"
    projectiles_dir = resources_root / "common" / "lycanitesmobs" / "projectiles"

    creature_model_base_dir = java_root / "com" / "lycanitesmobs" / "client" / "model" / "creature"
    projectile_model_base_dir = java_root / "com" / "lycanitesmobs" / "client" / "model" / "projectile"
    entity_base_dir = java_root / "com" / "lycanitesmobs" / "core" / "entity"

    if not java_root.is_dir():
        print(f"Java root not found: {java_root}")
        return
    if not entity_base_dir.is_dir():
        print(f"Entity base dir not found: {entity_base_dir}")
        return

    creature_model_map = find_classes(creature_model_base_dir, "com.lycanitesmobs.client.model.creature")
    projectile_model_map = find_classes(projectile_model_base_dir, "com.lycanitesmobs.client.model.projectile")
    entity_map = find_classes(entity_base_dir, "com.lycanitesmobs.core.entity")

    print(f"Found {len(creature_model_map)} creature model classes")
    print(f"Found {len(projectile_model_map)} projectile model classes")
    print(f"Found {len(entity_map)} entity classes")

    c_updated, c_total = process_folder(creatures_dir, creature_model_map, entity_map, "creatures")
    p_updated, p_total = process_folder(projectiles_dir, projectile_model_map, entity_map, "projectiles")

    print(f"All done. Creatures: {c_updated}/{c_total}, Projectiles: {p_updated}/{p_total}.")


if __name__ == "__main__":
    main()
