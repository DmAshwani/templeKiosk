package in.dataman.Enums;

import lombok.Getter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

@Getter
public enum ProjectServiceEvalType implements Serializable {
    FIX_VALUE(1, "Fix Value", "FX"),
    PER_UNIT(2, "Per Unit", "PU");

    private final int code;
    private final String name;
    private final String shortName;

    ProjectServiceEvalType(int code, String name, String shortName) {
        this.code = code;
        this.name = name;
        this.shortName = shortName;
    }

    public static Optional<ProjectServiceEvalType> getViaCode(int code) {
        return Arrays.stream(ProjectServiceEvalType.values())
                .filter(type -> type.getCode() == code)
                .findFirst();
    }

    public static Optional<ProjectServiceEvalType> getViaName(String name) {
        return Arrays.stream(ProjectServiceEvalType.values())
                .filter(type -> type.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public static Optional<ProjectServiceEvalType> getViaShortName(String shortName) {
        return Arrays.stream(ProjectServiceEvalType.values())
                .filter(type -> type.getShortName().equalsIgnoreCase(shortName))
                .findFirst();
    }

    public static String getShortNameByCode(int code) {
        for (ProjectServiceEvalType type : ProjectServiceEvalType.values()) {
            if (type.getCode() == code) {
                return type.getShortName();
            }
        }
        return null; // or throw an exception if preferred
    }


}

