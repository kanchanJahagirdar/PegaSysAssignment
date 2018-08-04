/**
 * @author Kanchan Jahagirdar
 * This script covers the testing of Login functionality
 * each case is handle using if condition
 *
 */

package com.selenium.login;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterTest;

public class WebScraperTesting {

	// Initialize Webdriver and required variables for execution
	public static WebDriver driver;
	static Properties prop;
	static String status="Notexecuted"; 
	
	public static void main(String[] args) {
		
		//Invoke methods for selecting browser and initializing property file
		prop=new Properties();  
	    browserSelect();
	    quitBrowser();
	}
	

	public static void browserSelect() 
	{
		// This method is used to load property file and select required browser
		String browser=null;
		InputStream input;
		try 
		{
			String projectLocation=System.getProperty("user.dir");
			input = new FileInputStream(projectLocation+"\\config.properties");
			prop.load(input);
			browser= prop.getProperty("browser");
			
			if (browser.equalsIgnoreCase("chrome"))
			{
				System.out.println("---Testing started on chrome browser---");
				System.setProperty("webdriver.chrome.driver",projectLocation+"\\chromedriver.exe");
				driver = new ChromeDriver();
				
			}
			if(browser.equalsIgnoreCase("firefox"))
			{
				System.out.println("---Testing started on mozilla browser---");
				driver = new FirefoxDriver();
			}
			doLogin(); // Invoke login method to load credentials and click on login button
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static void doLogin() throws IOException, InterruptedException
	{
			System.out.println("Do Login");	
			
			String cookieFlag = prop.getProperty("cookieMissing");
			
			driver.get(prop.getProperty("url"));	
			driver.findElement(By.name("usr")).sendKeys(prop.getProperty("usr"));	
			Thread.sleep(1000);
			driver.findElement(By.name("pwd")).sendKeys(prop.getProperty("pwd"));
			Thread.sleep(1000);
			driver.findElement(By.xpath("//*[@id=\"case_login\"]/form/input[3]")).click();
				
			//Below condition is meant to generate a scenario for invalid session cookie
			//THE SESSION COOKIE IS MISSING OR HAS A WRONG VALUE!
			
			if (cookieFlag.equalsIgnoreCase("true")) 
			{
				driver.manage().deleteAllCookies();
				driver.navigate().refresh();
			}
			
			doVerification(); // Invoke method for verifying login status
	}
	
	public static void doVerification() throws IOException, InterruptedException
	{
			System.out.println("Do Verification");
			Thread.sleep(5000);
			String Actual_Msg = driver.findElement(By.xpath("//*[@id=\"case_login\"]/h3")).getText();	
			System.out.println(Actual_Msg);
			//Comparing actual and expected message				 
			if (Actual_Msg.equals("WELCOME :)"))
			{
				//flag status is changed
				status= "Executed";
				System.out.println("Sucessfully Logged in");
				storeCookie();
			}
			else if (Actual_Msg.equals("ACCESS DENIED!"))
			{
				status= "Notexecuted";
				System.out.println("You cannot login as credentials are wrong");
			}
			else if (Actual_Msg.equals("THE SESSION COOKIE IS MISSING OR HAS A WRONG VALUE!"))
			{
				//flag status is changed to Addcookies
				status= "Addcookies";
				addCookie();
				
			}
			else if (Actual_Msg.contains("REDIRECTING"))
			{
				System.out.println("HTTP redirection was not processed");
				//flag status is changed to 
				status="HTTP 302";
				reDirect();
			}
			
	}
		
	public static void storeCookie() 
	{
		//If credentials are wrong then no need to store cookie
		//throw new SkipException("Skipping this exception");		    
		// create file named Cookies to store Login Information			
	    File file = new File("Cookies.data");							
	    try		
	    {	  
	        if(status.equalsIgnoreCase("Executed")) 
	        {
		    	// Delete old file if exists
		        System.out.println("Storing the cookies info");	
				file.delete();		
		        file.createNewFile();			
		        FileWriter fileWrite = new FileWriter(file);							
		        BufferedWriter Bwrite = new BufferedWriter(fileWrite);							
		        // loop for getting the cookie information 				
			        for(Cookie ck : driver.manage().getCookies())							
			        {			
			            Bwrite.write((ck.getName()+";"+ck.getValue()+";"+ck.getDomain()+";"+ck.getPath()+";"+ck.getExpiry()+";"+ck.isSecure()));																									
			            Bwrite.newLine();             
			        }	
		        
		        Bwrite.close();			
		        fileWrite.close();	
		        System.out.println("cookies stored");
	        }
		}
	    catch(Exception ex)
	    {		
	        ex.printStackTrace();			
	    }
	}

	public static void addCookie() throws IOException {
		
		 try{					    
			    if(status.equalsIgnoreCase("Addcookies")) {
			    	
				    System.out.println("Cookies are been added in session");
			        File file = new File("Cookies.data");							
			        FileReader fileReader = new FileReader(file);							
			        BufferedReader Buffreader = new BufferedReader(fileReader);							
			        String strline;			
			        while((strline=Buffreader.readLine())!=null){									
			        StringTokenizer token = new StringTokenizer(strline,";");									
			        while(token.hasMoreTokens()){					
			        String name = token.nextToken();					
			        String value = token.nextToken();					
			        String domain = token.nextToken();					
			        String path = token.nextToken();					
			        Date expiry = null;					
			        String val;
			        
			        if(!(val=token.nextToken()).equals("null"))
					{		
			        	expiry = new Date(val);					
			        }
			        
			        Boolean isSecure = new Boolean(token.nextToken()).								
			        booleanValue();		
			        Cookie ck = new Cookie(name,value,domain,path,expiry,isSecure);			
			        System.out.println(ck);
			        driver.manage().addCookie(ck);	        
			        // This will add the stored cookie to your current session		
			        //After adding the cookie, for it to be applied to the page you opened, a page refresh must be done right after adding it.
			        //driver.get("http://testing-ground.scraping.pro/login");
			        Thread.sleep(1000);
			        driver.navigate().refresh();
			        
		        }		
		     }	
		  }
		}
		catch(Exception ex){					
			        ex.printStackTrace();			
			        }	
		}
		
	public static void reDirect() {
		if(status.equalsIgnoreCase("HTTP 302")) {
			//Click GO BACK to start again
			driver.findElement(By.xpath("//*[@id=\"case_login\"]/a")).click();
		}
	 }
		
	 public static void quitBrowser() {
		 
		//driver.quit();
	 }
}

