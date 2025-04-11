package in.dataman.transactionDTO;

import java.util.Map;

import in.dataman.transactionEntity.Donation;
import lombok.Data;
@Data
public class DonationDTO {
	private String  fullName;
    private String panNumber;
    private String donationAmount;
    private String address1;
    private String address2;
    private Map<String,String> selectedCountry;
    private Map<String,String> selectedState;
    private Map<String,String> selectedDistrict;
    private String pincode;
    
    private String mobile;
    private String preparedDt;
    public Donation toDonation() {
		Donation donation = new Donation();
		donation.setAmount(this.donationAmount);
		donation.setName(this.fullName);
		donation.setAdd1(this.address1);
		donation.setAdd2(this.address2);
		donation.setCountryCode(this.selectedCountry.get("code"));
		donation.setStateCode(this.selectedState.get("code"));
		donation.setCityCode(this.selectedDistrict.get("code"));
		donation.setMobile(this.mobile);

		donation.setPin(this.pincode);
		donation.setIsdCode("+91");
		donation.setPan(this.panNumber);
		return donation;
	}
}
