package kr.dtimes.contract;

public class Contract {

    String contractTime;
    String ContractText;

    public Contract(String contractTime, String contractText) {
        this.contractTime = contractTime;
        ContractText = contractText;
    }

    public String getContractTime() {
        return contractTime;
    }

    public void setContractTime(String contractTime) {
        this.contractTime = contractTime;
    }

    public String getContractText() {
        return ContractText;
    }

    public void setContractText(String contractText) {
        ContractText = contractText;
    }
}
