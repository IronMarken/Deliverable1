package logic;

public enum Months {
	
    JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEPT, OCT, NOV, DEC;
	
	//Formatting output string
	@Override
	public String toString() {
		String value = name();
		return value.substring(0,1) + value.substring(1).toLowerCase(); 
	}
	
}
