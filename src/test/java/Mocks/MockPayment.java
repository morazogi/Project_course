package Mocks;

import DomainLayer.IPayment;

public class MockPayment implements IPayment {
    public MockPayment() {}
    public void processPayment(double payment, String creditCardNumber, String expirationDate, String backNumber) throws Exception {
        String creditCardType = getCreditCardType(creditCardNumber);
        if (payment < 0) {
            throw new Exception("Negative payment");
        }
        if (creditCardType == null) {
            throw new Exception("Unknown credit card company");
        }
        if (!isValidCardNumber(creditCardNumber)) {
            throw new Exception("Invalid credit card number");
        }
        if(!isValidExpirationDate(expirationDate)) {
            throw new Exception("Invalid expiration date");
        }
    }

    private String getCreditCardType(String creditCardNumber){

        String visaRegex        = "^4[0-9]{12}(?:[0-9]{3})?$";
        String masterRegex      = "^5[1-5][0-9]{14}$";
        String amexRegex        = "^3[47][0-9]{13}$";
        String dinersClubrRegex = "^3(?:0[0-5]|[68][0-9])[0-9]{11}$";
        String discoverRegex    = "^6(?:011|5[0-9]{2})[0-9]{12}$";
        String jcbRegex         = "^(?:2131|1800|35\\d{3})\\d{11}$";

        try {
            creditCardNumber = creditCardNumber.replaceAll("\\D", "");
            return (creditCardNumber.matches(visaRegex) ? "VISA" : creditCardNumber.matches(masterRegex) ? "MASTER" :creditCardNumber.matches(amexRegex) ? "AMEX" :creditCardNumber.matches(dinersClubrRegex) ? "DINER" :creditCardNumber.matches(discoverRegex) ? "DISCOVER"  :creditCardNumber.matches(jcbRegex) ? "JCB":null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isValidCardNumber(String creditCardNumber){

        try {
            creditCardNumber = creditCardNumber.replaceAll("\\D", "");
            char[]      ccNumberArry    = creditCardNumber.toCharArray();

            int         checkSum        = 0;
            for(int i = ccNumberArry.length - 1; i >= 0; i--){

                char            ccDigit     = ccNumberArry[i];

                if((ccNumberArry.length - i) % 2 == 0){
                    int doubleddDigit = Character.getNumericValue(ccDigit) * 2;
                    checkSum    += (doubleddDigit % 9 == 0 && doubleddDigit != 0) ? 9 : doubleddDigit % 9;

                }else{
                    checkSum    += Character.getNumericValue(ccDigit);
                }

            }

            return (checkSum != 0 && checkSum % 10 == 0);

        } catch (Exception e) {

            e.printStackTrace();

        }

        return false;
    }

    private boolean isValidExpirationDate(String expirationDate) {
        if (expirationDate.length() != 5) {
            return false;
        }
        String stringMonth = expirationDate.substring(0, 2);
        String stringYear = expirationDate.substring(3, 5);
        Integer month = new Integer(stringMonth);
        Integer year = new Integer(stringYear);
        return 1 <= month & month <= 12 & year >= 25 & (expirationDate.charAt(2) == '/' | expirationDate.charAt(2) == '.' | expirationDate.charAt(2) == '\'');
    }

}
