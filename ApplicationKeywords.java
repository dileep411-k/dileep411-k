package iSAFE;

import static io.restassured.RestAssured.given;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.entity.ContentType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import automationFramework.APIKeywords;
import automationFramework.GenericKeywords;
import baseClass.BaseClass;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import utilities.Mailing;
import utilities.ZipReportFile;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class ApplicationKeywords extends APIKeywords
{	
	public ApplicationKeywords(BaseClass obj) {
		super(obj);
	}
	
	
	public ApplicationKeywords()
	{

	}	
	
	// Sending email at the end
	public static void sendMail()
	{
		if(GenericKeywords.getConfigProperty("SendMail(Yes/No)").equalsIgnoreCase("yes"))
		{
			ZipReportFile.zipReport();
			//Mailing.sendAttachmentMail(fileName);
			Mailing.sendMail(GenericKeywords.outputDirectory+".zip");
		}
	}
	
	// Function Number 9:
    // This function can be used to convert strings like 'Today', 'Today+1' into their proper date values [in the appropriate format] 
	
 	// Example 1:
 	//String paramDateString = "Today";
 	//String paramDateFormat = "M/d/yyyy";
 	//String returnDate = GenericSelenium.returnFormattedDate(paramDateString, paramDateFormat);
 	// The above call would cause the current date to be returned in the 'M/d/yyyy' format like '9/27/2012' etc.

 	// Example 2:
 	//String paramDateString = "Today+1";
 	//String paramDateFormat = "dd MMM yyyy";
 	//String returnDate = GenericSelenium.returnFormattedDate(paramDateString, paramDateFormat);
 	// The above call would cause tomorrow's date to be returned in the 'dd MMM yyyy' format like '28 Sep 2012' etc.
    
 	// Example 3:
 	//String paramDateString = "Today-1";
 	//String paramDateFormat = "dd MMM yyyy";
 	//String returnDate = returnFormattedDate(paramDateString, paramDateFormat);
 	// The above call would cause yesterday's date to be returned in the 'dd MMMM yyyy' format like '26 Sep 2012' etc.
 	public String returnFormattedDate(String paramDateString, String paramDateFormat) {
 		
 		String returnDate="";
 		paramDateString = paramDateString.toLowerCase();
 		if (paramDateString.contains("today")) {
 			int offsetDays;
 			if (paramDateString.contains("+")) {	
 				// 'Java' throws an error when a string is attempted to be split using '+' 
 				// The error is 'Dangling meta character '+' near index 0 + ^'
 				// So, we are replacing '+' with ';' and  then splitting by ';' to get the 'offsetDays'
 				paramDateString = paramDateString.replace("+", ";");
 				String [] dateStringArray = paramDateString.split(";");
 				offsetDays = Integer.parseInt(dateStringArray[1]);
 			}else if (paramDateString.contains("-")) {
 				String [] dateStringArray = paramDateString.split("-");
 				offsetDays = Integer.parseInt(dateStringArray[1]);
 				// if the tester had given "today - 2", it means that '2' days should be reduced from the current date
 				// In order to reduce "2" days, "-2" should be passed to the the '.add' function
 				// To convert "2" to "-2", we are subracting the "2" from "100" and then subracting "100" from the 
 				// difference between "100" and "2", which is "98"
 				// int difference = 100 - 2 
 				// offsetDays = 98 - 100
 				// which would make 'offsetDays' as '-2'					
 				int difference = 100 - offsetDays;
 				offsetDays = difference - 100;
 			}else { // DateString is 'today'
 				offsetDays = 0;				
 			}	
 			Calendar calendar = Calendar.getInstance();
 			calendar.add(Calendar.DATE, offsetDays);
 			Date date = calendar.getTime();
 			Format formatter = new SimpleDateFormat(paramDateFormat);
 			returnDate = formatter.format(date).toString();
 		}else {
 			testStepFailed("When trying to get the formatted date, found that the option, '" + paramDateString + "' was not coded for");
 		}
 		return returnDate;
 	}
 	
	
	// API Functions 	
	// Feb. 8th 2019 - Vaidy
	// This function would be used to create a folder named 'Input' under the particular results directory
	// The entire folder path, including the folder name 'Input', would be passed as a parameter to the function 
	public void createInputParametersFolder(String paramResultsDirectory) {
		paramResultsDirectory = paramResultsDirectory.replace("/",  "\\"); // to make the directory path as "C:\TestResults\Input' when it is given as 'C:/TestResults/Input' 		
		File file = new File(paramResultsDirectory + "");
		if (! file.exists()) {
            file.mkdir();
        }		
	}
	
	
	// Feb. 8th 2019 - Vaidy
	// This function would be used to write the input parameters to a file
	// The input parameters would be given as an array like below:
	//parametersArray[0] = "Input Parameters for the 'AddOrUpdateClient' method:";
	//parametersArray[1] = "Username: <<<Username Value>>>";
	// and so on	
	public void saveInputParametersToFile(String inputParametersFileName, String[] parameterHeadersArray) {
		try {
			inputParametersFileName = inputParametersFileName.replace("/",  "\\"); // to make the file path as "C:\TestResults\Input\abcd.txt' when it is given as 'C:/Windows/Input/abcd.txt' 		
			File file = new File(inputParametersFileName);
			FileOutputStream fileOutputSteam = new FileOutputStream(file);			 
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputSteam));
			
			// parameterHeadersArray[0] = "Input Parameters for the 'AddOrUpdateClient' method:";
			// parameterHeadersArray[1] = "Username: <<<Username Value>>>;
			// parameterHeadersArray[2] = "Password: <<<Password Value>>>;
			// Each of the parameter header would be written in a separate line
			// with two line spaces following it
			int numberOfParameters = parameterHeadersArray.length - 1;
			int i;
			for (i=0; i<=numberOfParameters; i++) {
				bufferedWriter.write(parameterHeadersArray[i]);
				bufferedWriter.newLine(); 
				bufferedWriter.newLine();
			}
			bufferedWriter.close();
		} catch (IOException e) {
			stepFailed("Exception while writing input parameters to file - '" + e.getClass().getName() + "'"); // 'stepFailed' function would not take screenshot in the latest 'isafe' jar - iSAFE_3.0_Tactal_03052019.jar
		}
	}
	
	// Mar. 16th 2020 - Vaidy
	// In order for this API to work, we need to ignore the 'Security Certificate'
	// We are ignoring it using the '-k' option	
	public void ignoreSecurityCertificate() {
		// 'requestParameters' is the list that contains all the parameters of the 'CURL' command like:
		// CURL -i -X POST etc.
		// Adding '-k' to the list
		//requestparameters.add("-k ");
		
		
		//APIKeywords obj = new APIKeywords(this.obj);
		httpRequest.relaxedHTTPSValidation();
	}
	
	public void setContentType() {
		httpRequest.contentType("application/json");
	}
	
	
	// Mar. 13th 2020 - Vaidy
	// The 'API' methods of 'Bajaj Finserv' would accept the 'Paylod JSON' only in an encrypted form
	// So, we are encrypting it with a 'Public Key' and an 'Initialization Vector'	
	public String encryptJSON(String clearJSON, String publicKey, String initializationVector) {
		String encryptedJSON = null;
		try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(publicKey.getBytes(),"AES"), new IvParameterSpec(initializationVector.getBytes()));
            BASE64Encoder encoder = new BASE64Encoder();
            encryptedJSON = encoder.encode(cipher.doFinal(clearJSON.getBytes("UTF-8")));
            
    		// The Encrypted String is somehow given in multiple lines as below:
    		// q7BUOszoUdWv7MM3qerbIURzW4o2YvfclMjwOo6cb2dZPKbMGHXc+q0HU0TuFf/j0O6P31gSasdW
    		// dXM3vh/l/lWh3OErK2ea4aF0egLemm84iKMp2dwYFUX2T5bJC22YKZWZTV4h1bcvAKk78dDAOze2
    		// P2EWN5kVCFinYGPCibie8VEQMldXKrlfIChv7XvQ
    		// Somehow, the 'Seal' gets generated only when the encrypted string is in a single line as follows:
    		// q7BUOszoUdWv7MM3qerbIURzW4o2YvfclMjwOo6cb2dZPKbMGHXc+q0HU0TuFf/j0O6P31gSasdWdXM3vh/l/...
    		// So, we had removed the 'New Line Character' from the encrypted string to make it as one line
            encryptedJSON = encryptedJSON.replace("\r\n", "");	
            testStepPassed("Encrypted JSON: " + encryptedJSON);
        }catch (Exception e) {
            stepFailed("Error while encrypting: " + e.getClass().getName());
        }
        return encryptedJSON;
     }
	
	
	 // In addition to the encrypted JSON, the 'Bajaj Finserv API' methods also need something known as 'Seal'
	 // Seal is got by applying the 'MD5' hashing algorithm on the combination of the encrypted string and the 
	 // public key	
	 public String calculateSeal(String encryptedJSON, String publicKey) {
		 String encryptedJSONPlusKey = encryptedJSON + publicKey;
		 
		 String seal = null;
		 try {			 
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(encryptedJSONPlusKey.getBytes(), 0, encryptedJSONPlusKey.length());
			seal = new BigInteger(1, digest.digest()).toString(16);
			while (seal.length() < 32) { 
				seal = "0" + seal; 
	        } 
			testStepPassed("Seal: " + seal);
		}catch (Exception e) {
			stepFailed("Error while getting seal: " + e.getClass().getName());
		}
		return seal;
    }
	 
	// The encryptedResponse would look like below:
	// lAckimYZa1Pt8..A=|5b2eb6fac517a71190b3602e4cfb1a51
	// Before decrypting the response, we remove the portion after the delimiter '|'
	public String removeTextAfterDelimiterFromEncryptedResponse(String encryptedResponse) {	 
		encryptedResponse = encryptedResponse.substring(0, encryptedResponse.lastIndexOf("|")+1);
		testStepPassed("Encrypted response with text removed after '|' : " + encryptedResponse);
		return encryptedResponse;
	}
	 
	
	// The response would be given as encrypted
	// The below function can be used to decrypt the response   
    public String decryptResponse(String encryptedResponse, String encryptionKey, String initializationVector) {
    	String decryptedResponse = null;
    	try {
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey.getBytes(),"AES"), new IvParameterSpec(initializationVector.getBytes()));
	        BASE64Decoder decoder = new BASE64Decoder();      
	        decryptedResponse = new String(cipher.doFinal(decoder.decodeBuffer(encryptedResponse)));
	        testStepPassed("Decrypted response: " + decryptedResponse);
	    } catch (Exception e) {
	        stepFailed("Error while decrypting: " + e.getClass().getName());
	    }
	    return decryptedResponse;	    
    }
    
    
    // Mar. 20th 2020 - Vaidy
    // The 'API' functions are not getting executed with the default 'API' commands of 'iSafe'
    // So, we had created our own method as below
    // The below method would set the headers and the body before executing the 'API' function
    public Response executeAPIMethod(String apiURI, String apiHeaders, String apiBody) {
    		
		// For 'OTP', 'Auth' etc., there are only two headers - Content Type and Seal
		// But for certain other 'APIs', there are three headers
		// So, we have given 'line-separated' headers in the 'TestData.xls' file as below:
		// Content-Type,application json
		// SealValue,123123AFDasd
		// We form a list of headers with the line-separated headers read from the test data file
		// and set this headers list in the command   		
		// We do the following:
		// Let us assume that the line-separated headers in the test data file is as below:
		// Content-Type,application json
		// SealValue,seal
		// 1. Splitting by '\n' would cause headers array as follows:
		// headers array[0] = content-Type,application/json
		// headers-array[1] = SealValue,123123AFDasd
		// 2. Splitting each of the above headers array element by ',' would separate the header name and header value as below:
		// Content-Type and application/json
		// SealValue and 123123AFDasd
		// 3. Adding each of the header name and value into a big headers list
		// 4. Setting this headers list in the 'API' command via. the '.headers' function    		
    	String [] apiHeadersArray = apiHeaders.split("\\n");
		int numberOfApiHeaders = apiHeadersArray.length;
		int i;
		List<Header> headersList = new ArrayList<Header>();
		String [] individualHeaderArray;	
	    for (i=0;i<numberOfApiHeaders;i++) {
	    	individualHeaderArray = apiHeadersArray[i].split(","); 
	    	headersList.add(new Header(individualHeaderArray[0],individualHeaderArray[1]));
		}
	    
	    // At times, when the network or VPN is slow, an exception might be thrown
	    // So, instead of attempting execution just once, we are attempting six times 
	    // with a '10-second' wait between each attempts	    
	    for (i=1;i<=6;i++) {		
		    try {
			    response = null; // nullifying the response at the start		    
			    response = given()
				         .headers(new Headers(headersList))
				         .body(apiBody)
						 .relaxedHTTPSValidation() // In order for the 'API' to work, we need to ignore the security certificate
						 .when()
						 .post(apiURI);	
		    	testStepPassed("API command executed successfully");
		    	break;
	    	}catch (Exception e) {				
				if (i==6) {
	        		stepFailed("Exception while attempting to execute the API method  - '" + e.getClass().getName() + "'");
	        	}else {
	        		testStepInfo("Exception while attempting to execute the API method  - '" + e.getClass().getName() + "'");
	    			waitTime(10); // a small before trying again
	        	}				
			}
	    }
    	return response;
    }
    
    
    // Feb. 12th 2019 - Vaidy
	// This function can be used to validate the 'Response Code' in the API response
	// There is a method in 'iSafe' named 'validateStatus' for this purpose
	// but the 'iSafe' method demands that the name of the column containing the 'Response Code' in the 'TestData.xls' file should be 'Expected Status'
	// Since this is a limitation, had designed this method 
	public void validateResponseCode(int paramExpectedResponseCode) {
		int actualResponseCode = response.getStatusCode();		
		if(actualResponseCode == paramExpectedResponseCode) {
			testStepPassed("Response Code:");
			testStepPassed("Expected: " + paramExpectedResponseCode);
			testStepPassed("Actual: " + actualResponseCode);	
		}else {
			stepFailed("Response Code:");
			stepFailed("Expected: " + paramExpectedResponseCode);
			stepFailed("Actual: " + actualResponseCode);		 
		}
	}	
	
	
	// Mar. 16th 2020 - Vaidy
	// This function can be used to extract the encrypted response from the response file
	// Normally, the response would be given like below:
	// {"clientID":139618,"clientGUID":"c1gca195c551591","clientVendorNumber":"","clientName":"Ind_Client2_Jun21","isManualPayment":false,"callerID":"+12345678910111"}
	// Responses like this would be read by the 'getJSONResponse' method present in 'Application Keywords'
	// But, the encrypted response of 'BFL' would be given as below:
	// "lAckimYZa1Pt8BpnSrfqEerMw9r0jvcRWX7S0XlFz+7VTkh4T0yeVqx73sz79oqw4t6I1s04sa..."
	// Such a response would not be returned by the default 'getJSONResponse' method
	// So, we have used the below function to get the encrypted response from the output response file
	public String getEncryptedResponse() {		
		String encryptedResponse = response.getBody().asString();	
		
		// The encrypted response would be like:
		// 	"lAckimYZa1Pt8BpnSrfqEegbSCcCmpImLUqGfpecQh8cbZFlJW4T8bihHjW\/5gJwg5tz\/pFcmOYvCnh9spU2lhu8X\/PJhHVJy\/4hdwNiv9A=|4e26b3cbbfd6ca34eba63972cdfeba8d"
		// Somehow, the response gets decrypted only when '/' is removed
		// So, we are removing the '/'
		encryptedResponse = encryptedResponse.replace("/", "");	
				
		// Also, we are removing the "" present at the start and at the end
		encryptedResponse = encryptedResponse.substring(1,encryptedResponse.length()-1);
		
		if (! encryptedResponse.equals("")) {
			testStepPassed("Encrypted response: " + encryptedResponse);
		}else {
			stepFailed("No response could be found in the response file");
		}
		
		return encryptedResponse;
	}	
	
	
	// Mar. 16th 2020 - Vaidy
	// When a key is not present, the 'getJSONObjectValue' function in 'ApplicationKeywords' prints failure with a
	// blank screenshot [probably, it would have the 'testStepFailed' function]
	// So, we had overridden the function to print failure with the 'stepFailed' function
	@Override
	public String getJSONObjectValue(JSONObject paramResponseObject, String paramKey) {		
		String actualValue = null;
		try {
			// At times, response would be got as follows:
			/*		
			{
			   "CUSTINFO": [
			      {
			         "CUSTID": CUSTINFO.CN": "2030400701062126",
			         "MOBNO": "9702295121",
			         .
					 .
					 .
			      }
			   ],
			   "RESPDESC": "SUCCESS",
			   "RSPCODE": "00",
			   .
			   .
			   .
			}
			*/
			// Here 'CUSTID' and 'MOBNO' are keys of the parent key 'CUSTINFO'
			// To get their values, we need to first get the array of values of the parent 'CUSTINFO'
			// Then, we would get the values of 'CUSTID' and 'MOBNO' from the first element of the array	
			// The key of such values would be given in the 'TestData.xls' file as:
			// CUSTINFO.CUSTUD, CUSTINFO.MOBNO etc.
			// In these cases, we do the following:
			// 1. Separate the 'Parent Key' and the 'Child Key' by splitting with the dot
			// 2. Get the JSON response array of the parent key
			// 3. Get the first array element of the JSON array
			// 4. Get the value of the child key from the first array element
			if (paramKey.contains(".")) {			
				String [] expectedKeysArray = paramKey.split("\\."); // while splitting by '.', have to escape it using '\\'
				String parentKey = expectedKeysArray[0];
				String childKey = expectedKeysArray[1];				
				if (paramResponseObject.has(parentKey)) {
					JSONArray responseArray = getJSONArray(paramResponseObject, parentKey);
					JSONObject firstResponseObject = responseArray.getJSONObject(0);
					actualValue = firstResponseObject.getString(childKey);	
				}else {
					stepFailed("Key '" + parentKey + "' not present in the response '" + paramResponseObject.toString() + "'");			
				}				
			}else {			
				if (paramResponseObject.has(paramKey)) {
					actualValue = paramResponseObject.getString(paramKey);			
				}else {
					stepFailed("Key '" + paramKey + "' not present in the response '" + paramResponseObject.toString() + "'");			
				}
			}
		} catch (Exception e) {
			stepFailed("Exception '" + e.getClass().getName() + "' while attempting to get the value of the key '" + paramKey + " from the response '" + paramResponseObject.toString() + "'");
		}
		return actualValue;
		
	}
	
	
	// Jan. 23d 2020 - Vaidy
	// The 'validateJSONResponseObject' method in 'iSafe' validates key value pairs
	// but when there is a mismatch, it does not explicitly say that
	// It just says that the actual response for the key does not match the expected response
	// Also, it causes a blank screenshot to be displayed [it might have used the 'testStepFailed' method]
	// Moreover, when the value of the key contains a ':' like 'https://admin-qa2.icommconnect.com',
	// the validation fails
	// Also, it accepts the key/value pair only as 'domain:automation.com'
	// It does not accept it as "domain";"automation.com", which might be the way the key value pairs are given at times
	// To fix these points and to cause the 'Pass/Fail' to be printed in different lines, had overridden the function	
	@Override
	public void validateJSONResponseObject(JSONObject paramResponseObject, String paramExpectedKeyValuePair) {
		
		// Let us assume the below key/value pair:
		// Logo: https://admin-qa2.icommconnect.com
		// When this is split with ':', it would cause the resultant array to be as follows:
		// splitArray[0] - "Logo"
		// splitArray[1] = " https"
		// splitArray[2] = "admin-qa2.icommconnect.com"
		// The value would itself get split, which would lead to a failure
		// In order to prevent this, we are splitting the key/value pair only once with ':'
		// We are doing this by passing '2' to the 'limit' parameter of the 'split' command
		// This '2' would cause the split to be done only once
		// So, the resultant array would be proper as follows:
		// splitArray[0] - "Logo"
		// splitArray[1] = "https://admin-qa2.icommconnect.com"		
		String [] expectedKeyValueArray  = paramExpectedKeyValuePair.split(":", 2);		
		
		// At times, the key/value pair might be given with 'double quotes'as follows:
		// "clientName":"abcd"
		// We are removing the 'double quotes' from both the key and the value
		// We are also removing the leading space present in the value
		// And if the value is given as "" [like "IVRExt":""], we are blanking it out
		String expectedKey = expectedKeyValueArray[0].replace("\"", "");
		String expectedValue = expectedKeyValueArray[1].trim();		
		if (expectedValue.equals("\"\"")) {
			expectedValue = "";
		}else {				
			expectedValue = expectedValue.replace("\"", "");
		}
		
		String actualValue = getJSONObjectValue(paramResponseObject, expectedKey);
		
		// If the key is not present in the 'JSON', 'actualValue' would be null		
		if (actualValue != null) {	
			if (expectedValue.equals(actualValue)) {
				testStepPassed("Response Key: " + expectedKey);
				testStepPassed("Expected Value: " + expectedValue);
				testStepPassed("Actual Value: " + actualValue);				 
			}else {
				stepFailed("Response Key: " + expectedKey);
				stepFailed("Expected Value: " + expectedValue);
				stepFailed("Actual Value: " + actualValue);
			}
		}	
	}
	
	
	// Mar. 18th 2020 - Vaidy
 	// This function would be used to write the output responses to a file
	// This would be done by default in the old 'iSafe' jar that used 'CURL'
	// But in the new 'Rest Assured', this file needs to be created manually
 	public void saveOutputResponsesToFile(String outputResponsesFileName, String encryptedResponse, String decryptedResponse) {
 		try {			
 			
 			// In this file, we are going to write the following details:
 			// Response Status Line - HTTP 200 OK
 			// Response Headers like date, cache-control etc.
 			// Response Time in seconds
 			// Encrypted Response
 			// Decrypted Response
 			
 			// We are going to write each of these things under appropriate heading with
 			// two line spaces between each
 			
 			// Firstly, we would write the heading 'Report Status Line:' and the value of this in the next line
 			// Then after two line spaces, we would write the header 'Response Headers:' with each of the headers in separate lines
 			// Then, we would write the response time
 			// Then the encrypted response
 			// Lastly the decrypted response		
 			
 			// Creating the output responses file	
 			outputResponsesFileName = outputResponsesFileName.replace("/",  "\\"); // to make the file path as "C:\TestResults\Input\abcd.txt' when it is given as 'C:/Windows/Input/abcd.txt' 		
 			File file = new File(outputResponsesFileName);
 			FileOutputStream fileOutputSteam = new FileOutputStream(file);			 
 			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputSteam));
 			
 			// Response Status Line		
 			String responseStatusLine = response.getStatusLine(); 
 			bufferedWriter.write("Response Status:");
 			bufferedWriter.newLine(); 
 			bufferedWriter.write(responseStatusLine); 
 			
 			// Two line spaces 			
 			bufferedWriter.newLine(); 
 			bufferedWriter.newLine();
 			
	 		// Response Headers		
			Headers responseHeaders = response.headers();
			bufferedWriter.write("Response Headers:");
			for(Header responseHeader:responseHeaders) {
				bufferedWriter.newLine();
				bufferedWriter.write(responseHeader.toString());			
			}
			
 			// Two line spaces 			
 			bufferedWriter.newLine(); 
 			bufferedWriter.newLine();
 			
			// Response Time
			// We would get this in milliseconds and write it in seconds
			// For example, '703' milliseconds would be written as '0.703' seconds			
			long responseMilliSeconds = response.getTimeIn(TimeUnit.MILLISECONDS);
			bufferedWriter.write("Response Time:");
			bufferedWriter.newLine(); 
 			bufferedWriter.write(responseMilliSeconds/1000 + "." + responseMilliSeconds%1000 + " seconds");
			
 			// Two line spaces 			
 			bufferedWriter.newLine(); 
 			bufferedWriter.newLine();
 			
			// Encrypted Response		
 			bufferedWriter.write("Encrypted Response:");
 			bufferedWriter.newLine(); 
 			bufferedWriter.write(encryptedResponse);
 			
 			// Two line spaces 			
 			bufferedWriter.newLine(); 
 			bufferedWriter.newLine(); 			
			
			// Decrypted Response			
			bufferedWriter.write("Decrypted Response:");
 			bufferedWriter.newLine(); 
 			bufferedWriter.write(decryptedResponse); 
 			
 			// Saving the file
 			bufferedWriter.close();
 		} catch (IOException e) {
 			stepFailed("Exception while writing to output responses to file - '" + e.getClass().getName() + "'"); // 'stepFailed' function would not take screenshot in the latest 'isafe' jar - iSAFE_3.0_Tactal_03052019.jar
 		}
 	}
 	
 	
 	//Sample method created by Sabesan for testing
 	public void passOrFail(String status) {
 		if(status.equals("Pass")) {
 			testStepPassed("Application Keywords");
 		}
 		else if(status.equals("Fail")) {
 			stepFailed("Application Keywords");
 		}
 	}
}