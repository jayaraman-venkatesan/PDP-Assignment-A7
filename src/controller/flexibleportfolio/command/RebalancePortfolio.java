package controller.flexibleportfolio.command;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.jar.JarEntry;
import model.TransactionType;
import model.flexibleportfolio.FlexiblePortfolio;
import model.flexibleportfolio.FlexiblePortfolioImpl;
import model.flexibleportfolio.FlexiblePortfolioList;
import model.flexibleportfolio.PortfolioItemTransaction;
import model.portfolio.PortfolioItem;
import view.flexibleportfolio.FlexiblePortfolioView;

public class RebalancePortfolio implements FlexiblePortfolioControllerCommand {


  private final FlexiblePortfolioList fpList;

  private final FlexiblePortfolioView view;

  public RebalancePortfolio(FlexiblePortfolioList fpList, FlexiblePortfolioView view) {
    this.fpList = fpList;
    this.view = view;
  }


  private boolean validPortfolioName(String[] pNamesList, String pName) {
    return Arrays.asList(pNamesList).contains(pName);
  }


  /**
   * Method that contains the controller command implementation.
   *
   * @param scan
   */
  @Override
  public void goCommand(Scanner scan) throws IOException {

    String[] pNames = fpList.getPortfolioListNames();
    if (pNames.length < 1) {
      view.noPortfoliosMessage();
      return;
    }
    view.displayListOfPortfolios(pNames);

    view.portfolioNamePrompt();
    String pName = scan.next().toLowerCase();
    if (pName.equals("0")) {
      return;
    }

    if (!validPortfolioName(fpList.getPortfolioListNames(), pName)) {
      view.portfolioNameErrorMessage();
      return;
    }

    view.datePrompt();
    String dateString = scan.next();
    if (dateString.equals("0")) {
      return;
    }

    LocalDate date;
    try {
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
      date = LocalDate.parse(dateString, formatter);
    } catch (Exception e) {
      view.invalidDateStringMessage(dateString);
      return;
    }

    PortfolioItem[] portfolioItems;
    try {
      portfolioItems = fpList.getPortfolioCompositionAtDate(pName, date);
    } catch (Exception e) {
      view.displayErrorPrompt("Flexible Portfolio Composition on Date Failed! Error: " + e);
      return;
    }

    Map<String, Integer> percentageMappings = new HashMap<>();

    Map<String, Float> quantityMapping = new HashMap<>();

    int availablePercentage = 100;
    while (true) {
      int i = 0;
      availablePercentage = 100;
      for (; i < portfolioItems.length; i++) {
        System.out.println("Enter percentage for stock : " + portfolioItems[i]);
        quantityMapping.put(portfolioItems[i].getStock().getTicker(),
            portfolioItems[i].getQuantity());
        int percentage = scan.nextInt();
        if (percentage > availablePercentage) {
          System.out.println("Exceeded the limit. Available percentage " + availablePercentage);
          break;
        }
        availablePercentage -= percentage;
        if (availablePercentage == 0 && i != portfolioItems.length - 1) {
          System.out.println("Invalid percentage. Still have stocks left. Start again");
          break;
        }

        percentageMappings.put(portfolioItems[i].getStock().getTicker(), percentage);
      }

      if (availablePercentage == 0 && i == portfolioItems.length) {
        break;
      }

    }

    System.out.println(percentageMappings);

    fpList.rebalancePortfolio(pName, percentageMappings, date, quantityMapping, portfolioItems);

  }


}
