package tesPack;

import java.awt.Desktop.Action;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;


public class SiteNavigation {
	
	public static void main(String[] args) throws InterruptedException {
		
		System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
		
		WebDriver driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		driver.get("https://www.ss.com/");
		Thread.sleep(500);
		
		//	Selecting search parameters, first parameter - section, second - search quote
		SearchParameters("Электротехника", "Playstation4", driver);
		Thread.sleep(500);
		
		//	Selecting elements from drop-down panel, first parameter - name, second - title
		DropDownSelect("search_region", "Рига", driver);
		DropDownSelect("pr", "За последний месяц", driver);
		Thread.sleep(500);
		
		//	Pressing search button
		driver.findElement(By.xpath("//*[@id=\"sbtn\"]")).click();
		Thread.sleep(500);
		
		//	Sorting by price and selecting deal type in dropdown
		driver.findElement(By.xpath("//*[@id=\"head_line\"]/td[2]/noindex/a")).click();
		DropDownSelect("sid", "Продажа", driver);
		Thread.sleep(500);
		
		//	Opening extended search
		driver.findElement(By.xpath("//*[@id=\"page_main\"]/tbody/tr/td/table[1]/tbody/tr/td[4]/a")).click();
		Thread.sleep(500);
		
		//	Selecting min, max price and pressing search button
		MinMaxPrice("0", "300", driver);
		Thread.sleep(500);
		
		//	Selecting random bookmarks and saving them
		SavingAndOpeningBookmarks(10, driver);
		Thread.sleep(1500);
		
		//	Closing driver
		//driver.quit();
	}	
	
	//	Selecting search parameters
	private static void SearchParameters(String section, String searchQuote, WebDriver driver) {

		driver.findElement(By.xpath("//*[@id=\"main_table\"]/span[4]/a")).click();
		driver.findElement(By.linkText(section)).click();
		driver.findElement(By.xpath("//*[@id=\"main_table\"]/span[2]/b[3]/a")).click();
		driver.findElement(By.xpath("//*[@id=\"ptxt\"]")).sendKeys(searchQuote);
	}
	
	//	Selecting elements from drop-down panel
	private static void DropDownSelect(String dropDownName, String desiredElement,  WebDriver driver) {
		
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		
		WebElement dropDown = driver.findElement(By.name(dropDownName));
		List<WebElement> option = dropDown.findElements(By.tagName("option"));
		
		String element;
		for (int i=0; i<option.size(); i++) {
			element = option.get(i).getText();
			
			if (element.contentEquals(desiredElement)) {
				option.get(i).click();
				break;
			}
		}
	}
	
	//	Minimal and maximal price
	private static void MinMaxPrice(String minP, String maxP, WebDriver driver) {
		
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		
		driver.findElement(By.name("topt[8][min]")).sendKeys(minP);
		driver.findElement(By.name("topt[8][max]")).sendKeys(maxP);
		
		driver.findElement(By.xpath("//*[@id=\"sbtn\"]")).click();
	}
	
	//	Selecting elements from catalog ads list and saving them
	private static void SavingAndOpeningBookmarks(int checkCount, WebDriver driver) throws InterruptedException {
		
		driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
		
		//	Finding all checkboxes on page
		List<WebElement> checkboxes = driver.findElements(By.xpath("//input[@type='checkbox']"));
		//	Finding all titles on page
		List<WebElement> selectedTexts = driver.findElements(By.xpath("//td[@class='msg2']//div[@class='d1']"));
		//	Finding all urls on page
		List<WebElement> selectedLinks = driver.findElements(By.xpath("//div[@class='d1']//a[@class='am']"));
		//	Arrays for storing titles and urls
		List<String> selectedAds = new ArrayList<String>();
		List<String> selectedUrls = new ArrayList<String>();
		
		Actions actions = new Actions(driver);
		Random rand = new Random();
		
		//	Finding and selecting random checkboxes	
		boolean result = false;
		int attempts = 0;
		int amount = 0;
		for(WebElement el : checkboxes) {
			if(!el.isSelected() && amount <= checkCount) {
				amount++;
				Thread.sleep(500);
				while(attempts < 2) {
					try {
						actions.moveToElement(checkboxes.get(rand.nextInt(checkboxes.size()))).click().perform();
						result = true;
						break;
					} catch(StaleElementReferenceException e) { }
					attempts++;
				}
			}
			
		}
		
		//	Saving ads and urls
		for(int j = 0; j < checkboxes.size(); j++) {
			if(checkboxes.get(j).isSelected()) {
				//	We need to cropp ads titles so to be possible to compare them because of different lengths of ads titles on search page and in bookmarks
				selectedAds.add(selectedTexts.get(j).getText().toString().substring(0, 15));
				selectedUrls.add(selectedLinks.get(j).getAttribute("href").toString());
			}
		}
		
		//	Saving to bookmarks and opening bookmarks page
		actions.moveToElement(driver.findElement(By.id("a_fav_sel"))).click().perform();
		driver.findElement(By.linkText("Закладки")).click();
		
		//	Finding bookmarks titles and urls
		List<WebElement> bookmarksText = driver.findElements(By.className("d1"));
		List<WebElement> bookmarksUrl = driver.findElements(By.xpath("//div[@class='d1']//a[@class='am']"));
		//	Arrays for storing saved titles and urls
		List<String> savedTexts = new ArrayList<String>();
		List<String> savedUrls = new ArrayList<String>();

		//	Saving ads and urls
		for(int k = 0; k < bookmarksText.size(); k++) {
			//	Cropping ads titles
			savedTexts.add(bookmarksText.get(k).getText().toString().substring(0, 15));
			savedUrls.add(bookmarksUrl.get(k).getAttribute("href").toString());
		}

		//	Comparing ads titles
		for(String expectedText : savedTexts) {
			if(selectedAds.contains(expectedText)) {
				System.out.println("PASS - Selected and saved ad are equal: " + expectedText.toString());	
			} else {
				System.out.println("Comparing FAILED");
			}
		}
		
		System.out.println("==============================================");
		
		//Comparing ads urls
		for(String expectedUrl : savedUrls) {
			if(selectedUrls.contains(expectedUrl)) {
				System.out.println("PASS - Selected and saved url are equal: " + expectedUrl.toString());
			} else {
				System.out.println("Comparing FAILED");
			}
		}
	}
	
}
