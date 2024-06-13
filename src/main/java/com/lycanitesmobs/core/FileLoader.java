package com.lycanitesmobs.core;

import com.google.gson.*;
import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileLoader {
	public static FileLoader CLIENT;
	public static FileLoader SERVER;
	public static FileLoader COMMON;

	public enum Location {
		INTERNAL, CONFIG, REMOTE
	}

	private final String name;
	private final Location location;
	private final Gson gson;
	private FileSystem fileSystem;
	private String rootPath;
	public boolean ready = false;

	public FileLoader(String name, Location location) {
		this.name = name;
		this.location = location;
		this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
		try {
			if (location == Location.INTERNAL) {
				initializeInternalFileSystem();
			} else if (location == Location.CONFIG) {
				initializeConfigFileSystem();
			}
			this.ready = true;
		} catch (Exception e) {
			LycanitesMobs.logError("An exception occurred when creating a FileLoader for root path: " + this.rootPath);
			throw new RuntimeException(e);
		}
	}

	private void initializeInternalFileSystem() throws IOException, URISyntaxException {
		URI rootUri = null;
		try {
			rootUri = Objects.requireNonNull(this.getClass().getResource("/assets/" + this.name + "/.root")).toURI();
		} catch (Exception e) {
			LycanitesMobs.logError("Failed to get resource URI for /assets/" + this.name + "/.root");
			throw e;
		}

		LycanitesMobs.logDebug("FileLoader", "Root URI: " + rootUri);

		switch (rootUri.getScheme()) {
			case "file", "union" -> {
				Path rootFilePath = Paths.get(rootUri);
				String rootPathString = rootFilePath.toString().replace(rootFilePath.getFileName().toString(), "");
				this.rootPath = rootPathString;
				this.fileSystem = rootFilePath.getFileSystem();

				Path rootPath = rootFilePath.getFileSystem().getPath(rootPathString);
				copyJsonObjectsFromPath(rootPath);
			}
			case "modjar" -> {
				try (JarFile jar = new JarFile(getJarFile())) {
					Enumeration<JarEntry> entries = jar.entries();
					this.fileSystem = FileSystems.newFileSystem(rootUri, Collections.emptyMap());
					this.rootPath = this.name;
					copyJsonObjectsFromJar(entries);
				}
			}
			default -> throw new RuntimeException("Unsupported file scheme: " + rootUri.getScheme());
		}

		LycanitesMobs.logDebug("FileLoader", "Root Path: " + this.rootPath);
	}

	private void copyJsonObjectsFromPath(Path rootPath) throws IOException {
		Files.walk(rootPath)
				.filter(Files::isRegularFile)
				.filter(path -> path.toString().endsWith(".json"))
				.forEach(this::copyJsonFile);
	}

	private void copyJsonObjectsFromJar(Enumeration<JarEntry> entries) throws IOException {
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String entryName = entry.getName();
			if (entryName.startsWith(this.name + "/") && entryName.endsWith(".json") && !entryName.endsWith("/")) {
				try (InputStream inputStream = this.getClass().getResourceAsStream("/" + entryName)) {
					if (inputStream == null) {
						throw new RuntimeException("Error streaming file entry from mod jar: /" + entryName);
					}
					copyJsonFile(Paths.get(this.rootPath, entryName.substring((this.name + "/").length())), inputStream);
				}
			}
		}
	}

	private void copyJsonFile(Path targetPath) {
		try {
			if (!Files.exists(targetPath)) {
				Files.createDirectories(targetPath.getParent());
				Files.copy(this.getClass().getResourceAsStream("/" + targetPath.toString()), targetPath);
			}
		} catch (IOException e) {
			LycanitesMobs.logError("Error copying default config file: " + targetPath.toString());
			throw new RuntimeException(e);
		}
	}

	private void copyJsonFile(Path targetPath, InputStream inputStream) {
		try {
			if (!Files.exists(targetPath)) {
				Files.createDirectories(targetPath.getParent());
				Files.copy(inputStream, targetPath);
			}
		} catch (IOException e) {
			LycanitesMobs.logError("Error copying default config file: " + targetPath.toString());
			throw new RuntimeException(e);
		}
	}


	private void initializeConfigFileSystem() {
		String configRootPath = System.getProperty("user.dir") + "/config/" + this.name + "/";
		File rootPathFile = new File(configRootPath);

		if (!rootPathFile.exists()) {
			rootPathFile.mkdirs();
		}

		this.rootPath = rootPathFile.toPath().toString();
		this.fileSystem = FileSystems.getDefault();

		copyDefaultConfigsIfMissing("globalspawner.json");
		copyDefaultConfigsIfMissing("globalmobevent.json");
		copyDefaultConfigsIfMissing("mobeventschedule.json");

		copyDefaultConfigsDirectoryIfMissing("spawners", "data");
		copyDefaultConfigsDirectoryIfMissing("mobevents", "common");
		copyDefaultConfigsDirectoryIfMissing("creaturegroups", "common");
		copyDefaultConfigsDirectoryIfMissing("creatures", "common");
		copyDefaultConfigsDirectoryIfMissing("creaturetypes", "common");
		copyDefaultConfigsDirectoryIfMissing("dungeons", "data");
		copyDefaultConfigsDirectoryIfMissing("elements", "common");
		copyDefaultConfigsDirectoryIfMissing("equipment", "common");
		copyDefaultConfigsDirectoryIfMissing("items", "common");
		copyDefaultConfigsDirectoryIfMissing("projectiles", "common");
	}

	private void copyDefaultConfigsIfMissing(String fileName) {
		Path configFilePath = Paths.get(this.rootPath, fileName);
		if (!Files.exists(configFilePath)) {
			try (InputStream resourceStream = this.getClass().getResourceAsStream("/data/" + this.name + "/" + fileName)) {
				if (resourceStream == null) {
					LycanitesMobs.logError("Default config resource not found: /data/" + this.name + "/" + fileName);
					return;
				}
				Files.copy(resourceStream, configFilePath);
			} catch (IOException e) {
				LycanitesMobs.logError("Error copying default config file: " + fileName);
				throw new RuntimeException(e);
			}
		}
	}

	private void copyDefaultConfigsDirectoryIfMissing(String directoryName, String category) {
		try {
			Path configDirPath = Paths.get(this.rootPath, directoryName);
			if (!Files.exists(configDirPath)) {
				Files.createDirectories(configDirPath);
			}

			// Open the JAR file
			try (JarFile jarFile = new JarFile(getJarFile())) {
				// Iterate over the entries in the JAR
				Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String entryName = entry.getName();

					// Check if the entry is part of the target directory
					if (entryName.startsWith(category + "/" + this.name + "/" + directoryName + "/") && entryName.endsWith(".json")) {
						String relativePath = entryName.substring((category + "/" + this.name + "/").length());
						Path configFilePath = Paths.get(this.rootPath, relativePath);

						// Create subdirectories if they don't exist
						if (!Files.exists(configFilePath.getParent())) {
							Files.createDirectories(configFilePath.getParent());
						}

						// Copy the file if it doesn't exist
						if (!Files.exists(configFilePath)) {
							try (InputStream resourceStream = this.getClass().getResourceAsStream("/" + entryName)) {
								if (resourceStream == null) {
									LycanitesMobs.logError("Resource stream is null for: /" + entryName);
									continue;
								}
								Files.copy(resourceStream, configFilePath);
							} catch (IOException e) {
								LycanitesMobs.logError("Error copying default config file: " + entryName);
							}
						}
					}
				}
			} catch (IOException e) {
				LycanitesMobs.logError("Error reading entries from JAR file for directory: " + directoryName);
				throw new RuntimeException(e);
			}
		} catch (IOException e) {
			LycanitesMobs.logError("Error copying default config directory: " + directoryName);
			throw new RuntimeException(e);
		}
	}
	public static void initAll(String domain) {
		CLIENT = new FileLoader(domain, Location.INTERNAL);
		COMMON = new FileLoader(domain, Location.CONFIG);
		LycanitesMobs.logDebug("", "All FileLoaders initialised successfully.");
	}

	public Path getPath(String subPath) {
		return this.fileSystem.getPath(this.rootPath + "/" + subPath);
	}

	public List<ResourceLocation> getPathResourceLocations(String subPath, String fileType) {
		Path path = this.getPath(subPath);
		List<ResourceLocation> resourceLocations = new ArrayList<>();
		try {
			Iterator<Path> iterator = Files.walk(path).iterator();
			while (iterator.hasNext()) {
				Path filePath = iterator.next();
				if (fileType == null || fileType.equals(FilenameUtils.getExtension(filePath.toString()))) {
					Path relativePath = path.relativize(filePath);
					String resourceLocationPath = FilenameUtils.removeExtension(relativePath.toString()).replaceAll("\\\\", "/");
					ResourceLocation resourceLocation = new ResourceLocation(this.name, resourceLocationPath);
					resourceLocations.add(resourceLocation);
				}
			}
		} catch (Exception e) {
			LycanitesMobs.logWarning("", "There was a problem getting ResourceLocations for: " + path + ", " + fileType + ", " + " \n" + e.toString());
		}
		return resourceLocations;
	}

	public File getJarFile() {
		try {
			Path path = new File("mods").toPath();
			Iterator<Path> iterator = Files.walk(path).iterator();
			while (iterator.hasNext()) {
				Path filePath = iterator.next();
				if (filePath.getFileName().toString().toLowerCase().contains(this.name)) {
					return filePath.toFile();
				}
			}
			throw new RuntimeException("Unable to locate mod jar file.");
		} catch (Exception e) {
			LycanitesMobs.logError("Error locating mod jar file.");
			throw new RuntimeException(e);
		}
	}
}
