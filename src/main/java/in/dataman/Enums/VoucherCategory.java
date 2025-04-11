package in.dataman.Enums;


public enum VoucherCategory {
    DONATION("DO"),
    PUJA_BOOKING("PUJB"),
    TRUSTEE_PUJA_BOOKING("TPUJB"),
    QUEUE_BOOKING("QUE"),
    SAFE_DEPOSIT_BOX("SDB"),
    PRASAD_BOOKING("PRB");

    private final String code;

    VoucherCategory(String code) {
        this.code = code;
    }

    public String getShortName() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }
}




