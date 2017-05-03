package org.mybop.gradle;

import org.gradle.api.file.SourceDirectorySet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * In some contests you will have to submit all your code in a single file. This
 * class is here to help you build this unique file by scanning the base class
 * of your code, reading the imported/included classes, parse them and build
 * your file containing all your imported/included classes as private classes
 * (for java) in a unique file in the root directory
 * <p>
 * Usage: Run the main of this class and pass as argument the path of the file
 * where you have your main.
 * <p>
 * Example path : /src/builder/sample/Sample.java
 *
 * @author Manwe
 */
public final class FileBuilder {

    private static final String IMPORT = "import ";
    private static final String END_COMMENT = "*/";
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final Set<String> imports = new HashSet<>();

    private final Set<Path> knownFiles = new HashSet<>();

    private final Map<Path, ClassCode> innerClasses = new LinkedHashMap<>();

    private final SourceDirectorySet sourceRoot;

    FileBuilder(SourceDirectorySet sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    private boolean addLineToCode(final ClassCode code, boolean fileKeyWordRead, final String line) {
        if (line.startsWith("package ")) {
            // Do nothing, we'll remove the package info
        } else if (line.startsWith(IMPORT)) {
            final Optional<Path> importedClassPath = importToPath(line);

            if (!importedClassPath.isPresent()) {
                System.out.println("Standard import:" + line);
                imports.add(line);
            } else {
                innerClasses.put(importedClassPath.get(), processFile(importedClassPath.get()));
            }
        } else {
            if (fileKeyWordRead) {
                code.getAfterClassContent().add(line);
            } else {
                if (line.contains("abstract class ")) {
                    code.declaration(line, "abstract class ");
                    fileKeyWordRead = true;
                } else if (line.contains("class ")) {
                    code.declaration(line, "class ");
                    fileKeyWordRead = true;
                } else if (line.contains("interface ")) {
                    code.declaration(line, "interface ");
                    fileKeyWordRead = true;
                } else if (line.contains("enum ")) {
                    code.declaration(line, "enum ");
                    fileKeyWordRead = true;
                } else {
                    code.getBeforeClassContent().add(line);
                }
            }
        }
        return fileKeyWordRead;
    }

    private Optional<Path> importToPath(final String importStr) {
        final String className = importStr.substring(IMPORT.length())
                .replaceAll(";", "");
        return classNameToPath(className);
    }

    private Optional<Path> classNameToPath(final String className) {
        return sourceRoot.getSrcDirs().stream()
                .map(File::toPath)
                .map(dir -> Arrays.stream(className.split("\\.")).reduce(dir, Path::resolve, (p1, p2) -> {
                    throw new IllegalStateException("merge  " + p1 + " and " + p2);
                }))
                .map(path -> path.getParent().resolve(path.getFileName() + ".java"))
                .peek(path -> System.out.println("File path: " + path))
                .filter(Files::isRegularFile)
                .map(Path::toAbsolutePath)
                .findFirst();
    }

    public ClassCode processClass(final String className) {
        final Optional<Path> classPath = classNameToPath(className);
        if (classPath.isPresent()) {
            return processFile(classPath.get());
        } else {
            throw new IllegalArgumentException("class `" + className + "` not found");
        }
    }

    private ClassCode processFile(final Path fileName) {
        System.out.println("reading class content of " + fileName);
        knownFiles.add(fileName.toAbsolutePath());
        final List<String> fileContent = readFile(fileName);
        final ClassCode code = readFileContent(fileName, fileContent);
        readPackageClasses(fileName);

        return code;
    }

    private List<String> readFile(final Path fileName) {
        try {
            return Files.readAllLines(fileName, CHARSET);
        } catch (final IOException e) {
            System.err.println("Error while reading file " + fileName);
            throw new IllegalStateException("Unable to continue");
        }
    }

    private ClassCode readFileContent(final Path fileName, final List<String> fileContent) {
        final ClassCode code = new ClassCode(fileName);
        boolean fileKeyWordRead = false;
        boolean insideComment = false;
        for (final String line : fileContent) {
            final String trimedLine = line.trim();
            if (insideComment) {
                if (trimedLine.contains(END_COMMENT)) {
                    insideComment = false;
                    final String remainingCode = trimedLine.substring(trimedLine.indexOf(END_COMMENT) + END_COMMENT.length());
                    if (!remainingCode.trim().isEmpty()) {
                        fileKeyWordRead = addLineToCode(code, fileKeyWordRead, remainingCode);
                    }
                }
                // We can skip comments since generated file size might be
                // limited
            } else if (trimedLine.isEmpty()) {
                // We don't need empty lines
            } else if (trimedLine.startsWith("//")) {
                // We can skip comments since generated file size might be
                // limited
            } else if (trimedLine.startsWith("/*")) {
                // We can skip comments since generated file size might be
                // limited
                if (!trimedLine.contains(END_COMMENT)) {
                    insideComment = true;
                }
            } else {
                fileKeyWordRead = addLineToCode(code, fileKeyWordRead, line);
            }
        }
        return code;
    }

    private void readPackageClasses(final Path fileName) {
        try (final DirectoryStream<Path> ds = Files.newDirectoryStream(fileName.getParent())) {
            for (final Path child : ds) {
                final Path absolutePath = child.toAbsolutePath();
                if (!Files.isDirectory(child) && absolutePath.endsWith(".java") && !knownFiles.contains(absolutePath)) {
                    innerClasses.put(absolutePath, processFile(absolutePath));
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void write(final Path outputDir, final ClassCode treated) {
        final String className = Optional.ofNullable(treated.className())
                .filter(name -> !name.isEmpty())
                .orElse("Out");

        final Path outputFile = outputDir.resolve(className + ".java");

        final List<String> lines = new ArrayList<>();
        lines.addAll(imports);
        lines.addAll(treated.getBeforeClassContent());
        lines.add("class " + treated.className() + " {");
        for (final ClassCode innerClass : innerClasses.values()) {
            for (final String line : innerClass.getBeforeClassContent()) {
                lines.add("\t" + line);
            }
            lines.add("\tprivate static " + innerClass.declaration() + " {");
            for (final String line : innerClass.getAfterClassContent()) {
                lines.add("\t" + line);
            }
        }

        lines.addAll(treated.getAfterClassContent());

        try {
            Files.createDirectories(outputDir);
            Files.write(outputFile, lines, CHARSET, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
