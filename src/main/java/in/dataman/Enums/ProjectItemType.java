package in.dataman.Enums;

import lombok.Getter;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

@Getter
public enum ProjectItemType implements Serializable {
	PUJA_GENERAL("PG"), PUJA_TRUSTEE("PT"), LOCKER("LC"), FINISHED_MTRL("FM");

	private final String manualCode;

	ProjectItemType(String manualCode) {
		this.manualCode = manualCode;
	}

	public static Optional<ProjectItemType> getViaManualCode(String manualCode) {
		return Arrays.stream(ProjectItemType.values()).filter(item -> item.getManualCode().equalsIgnoreCase(manualCode))
				.findFirst();
	}
}
