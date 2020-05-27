package utilities;

import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;

public class RestUtils {
	
	public static String idTransaction()
	{
		return RandomStringUtils.randomNumeric(5);
	}
	
	public static String personalName()
	{
		String generatedString = RandomStringUtils.randomAlphabetic(3);
		return ("Robert " + generatedString);
	}
	
	public static String personalAge()
	{
		String generatedString = RandomStringUtils.randomNumeric(2);
		return (generatedString);
	}
	
	public static String personalEmail()
	{
		String generatedString = "test"+RandomStringUtils.randomNumeric(2)+"@test.com";
		return (generatedString);
	}
	
	public static String personalActive()
	{
		Random random = new Random();
		String[] pool = {"true","false"};
		int index = random.nextInt(pool.length);
		String generatedString = pool[index];
		return (generatedString);
	}

}
