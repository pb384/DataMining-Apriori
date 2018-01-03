import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class AprioriAlgorithm {

	private static AprioriAlgorithm apriori;

	private static final String COMMA = ",";

	private static final String SPACE_REGEX = "\\s+";

	/** List containing Item Set **/
	private List<List<ItemSet>> allItemSet;

	/** List of previous Item Set **/
	private List<ItemSet> prevItemList;

	/** List of current item set **/
	private List<ItemSet> currItemList;

	/** List of transactions read from file.**/
	private List<String> transactionList;

	/** List of items used in the transactions.**/
	private List<String> itemList;

	/** minimum support**/
	private int minimumSupport;

	/** minimum Confidence **/
	private int minimumConfidence;

	/** List stores all association rules **/
	private List<AssociationRule> associationRules;

	/** Private constructor of aprioriAlgorithm to implement a pattern. **/
	private AprioriAlgorithm() {
		itemList = new ArrayList<String>();
		currItemList = new ArrayList<ItemSet>();
		prevItemList = new ArrayList<ItemSet>();
		transactionList = new ArrayList<String>();
		allItemSet = new ArrayList<List<ItemSet>>();
		associationRules = new ArrayList<AssociationRule>();
	}

	/** This static method is used to get instance of aprioriAlgorithm
	 * @return {@link AprioriAlgorithm} **/
	public static AprioriAlgorithm getInstance() {
		if (apriori == null) {
			apriori = new AprioriAlgorithm();
		}
		return apriori;
	}

	/** This method is used to handle all methods and generate rules. 
	 * @param minimumSupport
	 *            minimum support in percent specified by user
	 * @param minimumConfidence
	 *            minimum confidence in percent specified by user
	 * @param fileName
	 *            File name from which transactions are present **/
	public void toGenerateRules(int minimumSupport, int minimumConfidence,
			String fileName) {

		toReadTransactions(fileName);
		toCalculateSupportConfidence(minimumSupport, minimumConfidence);

		System.out.println();
		System.out.println("Minimum Support Count: " + this.minimumSupport);
		System.out.println("Minimum Confidence : " + this.minimumConfidence);
		System.out.println();

		System.out.print("Items all together present in 20 transactions are :   ");
		String itemString = "";
		for (String item : itemList) {
			itemString += item + COMMA;
		}

		itemString = itemString.substring(0, itemString.length() - 1);
		System.out.println(itemString);

		System.out.println();
		Collections.sort(currItemList, new ItemListComparator());
		System.out.println("C1\n");
		toPrintCurrItemsList();
		System.out.println();
		toRemoveNotFreqItems();
		System.out.println("L1\n");
		toPrintCurrItemsList();

		int stage = 2;
		while (true) {
			prevItemList.clear();
			prevItemList.addAll(currItemList);
			currItemList.clear();

			if (stage != 2) {
				List<ItemSet> ItemSets = new ArrayList<ItemSet>(
						prevItemList);
				allItemSet.add(ItemSets);
			}

			if (!toMergeItems(stage)) {
				break;
			}
			toFindFreq();
			System.out.println();
			Collections.sort(currItemList, new ItemListComparator());
			System.out.println("C" + stage + "\n");
			toPrintCurrItemsList();
			System.out.println();
			toRemoveNotFreqItems();
			System.out.println("L" + stage + "\n");
			toPrintCurrItemsList();
			stage++;
		}
		toGenerateAssociationRules();

	}

	/** This method generates association rules **/
	
	public void toGenerateAssociationRules() {

		toRemoveInactiveRules();
		for (ItemSet ItemSet : prevItemList) {
			toGenerateRule(ItemSet);
		}

		System.out.println();
		System.out.println("Association Rules generated from frequent itemsets : ");
		System.out.println();
		for (AssociationRule rule : associationRules) {
			System.out.println(rule);
		}

		System.out.println();
		System.out.println("Association Rules generated from frequent itemsets that meet minimum Confidence : ");
		System.out.println();
		for (AssociationRule rule : associationRules) {
			if (rule.getConfidence() >= minimumConfidence) {
				System.out.println(rule);
			}
		}

	}

	/**This method generates association rules
	 * @param items **/
	
	public void toGenerateRule(ItemSet ItemSet) {
		
		String[] items = ItemSet.getItem().split(COMMA);
		List<String> subsets = toGenerateSubset(items);

		for (int i = 0; i < subsets.size(); i++) {

			String leftSide = subsets.get(i);
			for (int j = 0; j < subsets.size(); j++) {
				String rightSide = subsets.get(j);
				if (ToCheckLeftRightSideItems(leftSide, rightSide, ItemSet.getItem().split(COMMA).length)) {
					AssociationRule associationRule = new AssociationRule(leftSide, rightSide, ItemSet.getItem());
					toCalConfidence(associationRule);
					associationRules.add(associationRule);
				}
			}
		}
	}

	/**This method checks for left and right hand side item.
	 * @param leftSide
	 *            left side item.
	 * @param rightSide
	 *            right side item.
	 * @return true if right and left side is proper. false if right and
	 *         left hand side is not proper **/
	public boolean ToCheckLeftRightSideItems(String leftSide,
			String rightSide, int length) {

		boolean flag = true;

		String[] leftSideItems = leftSide.split(COMMA);
		String[] rightSideItems = rightSide.split(COMMA);

		if (leftSideItems.length + rightSideItems.length != length) {
			return false;
		}

		outer: for (String leftItem : leftSideItems) {

			for (String rightItem : rightSideItems) {
				if (leftItem.equals(rightItem)) {
					flag = false;
					break outer;
				}
			}

		}

		return flag;
	}

	/**This method generates subset.
	 * @param items
	 *            items
	 * @return List of subsets **/
	public List<String> toGenerateSubset(String[] items) {
		List<String> subsets = new ArrayList<String>();
		int value = (int) Math.pow(2, items.length) - 1;

		for (int i = 1; i < value; i++) {
			String subset = "";
			String binaryValue = Integer.toBinaryString(i);

			while (binaryValue.length() != items.length) {
				binaryValue = "0" + binaryValue;
			}

			for (int j = 0; j < binaryValue.length(); j++) {

				switch (binaryValue.charAt(j)) {
				case '1':
					subset += items[j] + COMMA;
					break;
				}

			}
			subset = subset.substring(0, subset.length() - 1);
			subsets.add(subset);
		}
		return subsets;
	}

	/**This method calculates confidence.
	 * @param rule
	 *            {@link AssociationRule}
	 * @return confidence **/
	public void toCalConfidence(AssociationRule rule) {
		double ruleConfidence = toCalculateFreq(rule.getRule());
		double leftSideConfidence = toCalculateFreq(rule.getleft());
		rule.setConfidence((ruleConfidence / leftSideConfidence) * 100.0);

	}

	/** This method removes inactive rules **/
	public void toRemoveInactiveRules() {
		for (Iterator<ItemSet> iterator = prevItemList.iterator(); iterator.hasNext();) {
			if (!iterator.next().isActive()) {
				iterator.remove();
			}
		}

	}

	/** This method prints current Item set list */
	public void toPrintCurrItemsList() {

		for (ItemSet ItemSet : currItemList) {
			if (ItemSet.isActive()) {
				System.out.println(ItemSet);
			}

		}
	}

	/** This method calculates frequency of the items */
	public void toFindFreq() {

		for (ItemSet ItemSet : currItemList) {
			int frequency = toCalculateFreq(ItemSet.getItem());
			ItemSet.setFreq(frequency);
		}

	}

	/** This method merges previous items.
	 * @param stage
	 *            value of n.
	 * @return true if items are successfully merged. false if not able to merge item */
	public boolean toMergeItems(int stage) {

		boolean mergeFlag = false;

		for (int i = 0; i < prevItemList.size(); i++) {

			ItemSet ItemSetOne = prevItemList.get(i);

			if (!ItemSetOne.isActive()) {
				continue;
			}

			for (int j = i + 1; j < prevItemList.size(); j++) {
				ItemSet ItemSetTwo = prevItemList.get(j);

				String mergedString = merge(ItemSetOne.getItem(),
						ItemSetTwo.getItem(), stage);
				if (!mergedString.isEmpty()
						&& toCheckActiveItems(mergedString)) {
					mergeFlag = true;
					ItemSet ItemSet = new ItemSet(mergedString, 1);
					if (!currItemList.contains(ItemSet)) {
						currItemList.add(ItemSet);
					}
				}
			}
		}

		return mergeFlag;

	}

	/** After merging items, this method checks it is in active items.
	 * @param item
	 *            current item.
	 * @return true if inactive items are not present. false if inactive items are present **/
	public boolean toCheckActiveItems(String item) {

		boolean flag = true;

		outer: for (List<ItemSet> ItemSets : allItemSet) {

			for (ItemSet ItemSet : ItemSets) {

				if (!ItemSet.isActive()) {

					String items[] = item.split(COMMA);

					String value = ItemSet.getItem();
					String values[] = value.split(COMMA);

					int count = 0;

					for (String ItemItem : items) {

						if (value.contains(ItemItem)) {
							count++;
						}
					}

					if (count == values.length) {
						flag = false;
						break outer;
					}

				}
			}
		}

		return flag;
	}

	/**This method merges two items.
	 * @param itemOne
	 *            item one
	 * @param itemTwo
	 *            item two
	 * @param stage
	 *            stage number
	 * @return merged string **/
	public String merge(String itemOne, String itemTwo, int stage) {

		String mergedItem = "";

		String[] itemOneArray = itemOne.split(COMMA);
		String[] itemTwoArray = itemTwo.split(COMMA);

		List<String> items = new ArrayList<String>(Arrays.asList(itemOneArray));

		for (String item : itemTwoArray) {

			if (!items.contains(item)) {
				items.add(item);
			}
		}

		if (items.size() == stage) {

			Collections.sort(items);

			for (String item : items) {
				mergedItem += item + COMMA;
			}
			mergedItem = mergedItem.substring(0, mergedItem.length() - 1);
		}

		return mergedItem;

	}

	/**This method calculates frequency of the current item.
	 * @param items
	 *            items whose frequency needs to be found.
	 * @return count of frequency **/
	public int toCalculateFreq(String item) {

		int count = 0;

		for (String transaction : transactionList) {

			String[] items = item.split(COMMA);

			boolean flag = true;

			for (String singleItem : items) {
				if (!transaction.contains(singleItem)) {
					flag = false;
					break;
				}
			}

			if (flag) {
				count++;
			}
		}

		return count;

	}

	/** This method removes non frequent item sets **/
	public void toRemoveNotFreqItems() {

		for (ItemSet ItemSet : currItemList) {
			if (ItemSet.isActive()
					&& ItemSet.getFreq() < this.minimumSupport) {
				ItemSet.setActive(false);
			}
		}

	}

	/**This method computes support and confidence in actual values.
	 * @param minimumSupport
	 *            minimum support in percent.
	 * @param minimumConfidence
	 *            minimum confidence in percent*/
	public void toCalculateSupportConfidence(int minimumSupport,
			int minimumConfidence) {

		Double support = (minimumSupport / 100.0) * transactionList.size();

		this.minimumSupport = support.intValue();
		this.minimumConfidence = minimumConfidence;
	}

	/** This method reads transactions. 
	 * @param fileName
	 *            Name of file in the directory **/
	public void toReadTransactions(String fileName) {

		File file = new File(fileName);
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
			String myline = null;
			while ((myline = bufferedReader.readLine()) != null) {

				String[] transcation = myline.split(SPACE_REGEX);

				transactionList.add(transcation[1]);
				String[] items = transcation[1].split(COMMA);

				for (String item : items) {
					ItemSet ItemSet = new ItemSet(item, 1);
					if (!itemList.contains(item)) {
						itemList.add(item);
						currItemList.add(ItemSet);
					} else {
						int index = currItemList.indexOf(ItemSet);
						ItemSet = currItemList.get(index);
						ItemSet.setFreq(ItemSet.getFreq() + 1);
					}
				}
			}

			bufferedReader.close();

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error while reading file");
		}

	}

	/**Comparator for the Ascending order of frequency**/
	private class ItemListComparator implements Comparator<ItemSet> {

		public int compare(ItemSet ItemSetOne, ItemSet ItemSetTwo) {
			return ItemSetOne.getFreq().compareTo(ItemSetTwo.getFreq());
		}
	}

}


