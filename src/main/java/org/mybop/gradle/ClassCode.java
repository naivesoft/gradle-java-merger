package org.mybop.gradle;

import java.util.ArrayList;
import java.util.List;

public final class ClassCode {
    private final String classFile;

    private String className;
    private String keyword;

    private final List<String> beforeClassContent = new ArrayList<>();
    private final List<String> afterClassContent = new ArrayList<>();

    ClassCode(String classFile) {
        this.classFile = classFile;
    }

    String className() {
        return className;
    }

    String declaration() {
        return keyword + className;
    }

    void declaration(String line, String keyword) {
        className = extractDeclaration(line, keyword);
        this.keyword = keyword;
    }

    List<String> getBeforeClassContent() {
        return beforeClassContent;
    }

    List<String> getAfterClassContent() {
        return afterClassContent;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClassCode other = (ClassCode) obj;
        if (classFile == null) {
            if (other.classFile != null) {
                return false;
            }
        } else if (!classFile.equals(other.classFile)) {
            return false;
        }
        return true;
    }

    private String extractDeclaration(String line, String str) {
        return line.substring(line.indexOf(str) + str.length()).replaceAll("\\{", "").trim();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((classFile == null) ? 0 : classFile.hashCode());
        return result;
    }
}
