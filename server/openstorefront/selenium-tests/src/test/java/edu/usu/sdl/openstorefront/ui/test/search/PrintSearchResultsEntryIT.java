/*
 * Copyright 2017 Space Dynamics Laboratory - Utah State University Research Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.usu.sdl.openstorefront.ui.test.search;

import edu.usu.sdl.apiclient.ClientAPI;
import edu.usu.sdl.openstorefront.common.exception.AttachedReferencesException;
import edu.usu.sdl.openstorefront.selenium.provider.AttributeProvider;
import edu.usu.sdl.openstorefront.selenium.provider.ClientApiProvider;
import edu.usu.sdl.openstorefront.selenium.provider.ComponentProvider;
import edu.usu.sdl.openstorefront.selenium.provider.ComponentTypeProvider;
import edu.usu.sdl.openstorefront.selenium.provider.OrganizationProvider;
import edu.usu.sdl.openstorefront.ui.test.BrowserTestBase;
import edu.usu.sdl.openstorefront.ui.test.admin.AdminTestBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author ccummings
 */
public class PrintSearchResultsEntryIT
		extends AdminTestBase
{

	private static final Logger LOG = Logger.getLogger(BrowserTestBase.class.getName());
	private ClientApiProvider provider;
	private ComponentProvider componentProvider;
	private OrganizationProvider orgProvider;
	private String entryName = "SeleniumTest";
	private String entryOrg = "Selenium Organization";

	@Before
	public void setup()
	{
		provider = new ClientApiProvider();
		ClientAPI apiClient = provider.getAPIClient();
		orgProvider = new OrganizationProvider(apiClient);
		orgProvider.createOrganization(entryOrg);
		componentProvider = new ComponentProvider(new AttributeProvider(apiClient), orgProvider, new ComponentTypeProvider(apiClient), apiClient);
		componentProvider.createComponent(entryName, "Selenium Entry for Print Search Result test", entryOrg);
	}

	@Test
	public void printEntryFromSearchResults()
	{
		for (WebDriver driver : webDriverUtil.getDrivers()) {

			searchAndClickEntry(driver, entryName);
			selectCustomTemplate(driver);
		}
	}

	public void searchAndClickEntry(WebDriver driver, String entryName)
	{
		webDriverUtil.getPage(driver, "Landing.action");

		WebDriverWait wait = new WebDriverWait(driver, 8);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".home-search-field-new"))).sendKeys("SeleniumTest");
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".x-btn.x-unselectable.x-box-item.x-btn-default-small"))).click();

		driver.navigate().refresh();

		List<WebElement> entryResults = new ArrayList<>();

		long startTime = System.currentTimeMillis();

		while (entryResults.isEmpty() && (System.currentTimeMillis() - startTime) < 60000) {

			entryResults = driver.findElements(By.cssSelector("#resultsDisplayPanel-innerCt h2"));

			if (entryResults.isEmpty()) {

				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".x-btn.x-unselectable.x-box-item.x-btn-default-large"))).click();
			}
		}

		boolean isResult = false;

		for (WebElement entry : entryResults) {

			System.out.println("Entry Results Name: " + entry.getText());

			if (entry.getText().equals(entryName)) {
				isResult = true;
				entry.click();
				break;
			}
		}

		Assert.assertTrue(isResult);
	}

	protected void selectCustomTemplate(WebDriver driver)
	{
		WebDriverWait wait = new WebDriverWait(driver, 15);

		String winHandleBefore = driver.getWindowHandle();

		WebElement frame = driver.findElement(By.cssSelector("iframe"));

		driver.switchTo().frame(frame);

		sleep(500);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-qtip = 'Print']"))).click();

		Set<String> handles = driver.getWindowHandles();
		List windows = new ArrayList(handles);
		String printWindow = (String) windows.get(windows.size() - 1);
		driver.switchTo().window(printWindow);

		sleep(1000);

		WebDriverWait wait10 = new WebDriverWait(driver, 10);
		WebElement element = wait10.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#customTemplateBtn")));
		element.click();

		List<WebElement> templateItems = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".x-menu-body.x-menu-body.x-unselectable .x-menu-item-text.x-menu-item-text-default.x-menu-item-indent-no-separator")));

		for (WebElement item : templateItems) {

			if (item.getText().equals("Description")) {
				item.click();
			}
		}

		driver.findElement(By.cssSelector("#customTemplateBtn")).click();

		driver.findElement(By.cssSelector(".x-body")).click();

		List<WebElement> templateSections = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#contentInfo-innerCt div h3")));

		boolean isSection = false;

		for (WebElement section : templateSections) {

			if (section.getText().equals("Description:")) {
				isSection = true;
			}
		}

		Assert.assertFalse(isSection);

		WebElement printBtn = driver.findElement(By.cssSelector("#printCustomizedEntryBtn"));

		boolean canPrint = false;

		if (printBtn.isDisplayed() && printBtn.isEnabled()) {
			canPrint = true;
		}

		Assert.assertTrue(canPrint);

		driver.close();

		driver.switchTo().window(winHandleBefore);
	}

	@After
	public void cleanupTest() throws AttachedReferencesException
	{
		componentProvider.cleanup();
	}

}
