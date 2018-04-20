
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;;

public class SalesAggregator {
	
	static Map <String, Double> totalIncomingSettledPerEntity = new HashMap<String, Double>(); //Sell
	static Map <String, Double> totalOutgoingSettledPerEntity = new HashMap<String, Double>(); //Buy
	
	static Map <Date, Double> totalIncomingSettledPerDate = new HashMap<Date, Double>(); //Sell
	static Map <Date, Double> totalOutgoingSettledPerDate = new HashMap<Date, Double>(); //Buy
	
	public static void main(String args[]) throws Exception{
	
		final String SALES_INSTRUCTIONS_FILE = "SalesInstructions.xls"; 
		
		Workbook salesInstructionsWorkbook = Workbook.getWorkbook(new File(SALES_INSTRUCTIONS_FILE));
		
		Sheet sheet = salesInstructionsWorkbook.getSheet(0);
		
		for (int i = 0; i < sheet.getRows(); i++) {
            	
            	if(sheet.getRow(i) != null && sheet.getRow(i).length > 0 && !"Entity".equalsIgnoreCase(sheet.getRow(i)[1].getContents()))
            		executeIndividualSale(sheet.getRow(i));
            	
        }
      
		Map <String, Double> totalRankedIncomingSettledPerEntity = sortByComparator(totalIncomingSettledPerEntity);
		Map <String, Double> totalRankedOutgoingSettledPerEntity = sortByComparator(totalOutgoingSettledPerEntity);
		
		int incomingRank = 1;
		int outgoingRank = 1;
		
		System.out.println("RANKED TOTAL INCOMING SETTLED BY ENTITY:");
		for (Map.Entry<String, Double> entry : totalIncomingSettledPerEntity.entrySet())
		{
	
		    System.out.println(entry.getKey() + "/" + entry.getValue() + " RANK#" + incomingRank);
		    incomingRank = incomingRank + 1;
		}
		
		System.out.println("RANKED TOTAL OUTGOING SETTLED BY ENTITY:");
		for (Map.Entry<String, Double> entry : totalOutgoingSettledPerEntity.entrySet())
		{
		    System.out.println(entry.getKey() + "/" + entry.getValue() + " RANK#" + outgoingRank);
		    outgoingRank = outgoingRank + 1;
		}
		
		System.out.println("AMOUNT SETTLED INCOMING PER DAY:");
		for (Map.Entry<Date, Double> entry : totalIncomingSettledPerDate.entrySet())
		{
		    System.out.println(entry.getKey() + "/" + entry.getValue());
		}
		System.out.println("AMOUNT SETTLED OUTGOING PER DAY:");
		for (Map.Entry<Date, Double> entry : totalOutgoingSettledPerDate.entrySet())
		{
		    System.out.println(entry.getKey() + "/" + entry.getValue());
		}
		
	
	}
	
	public static void executeIndividualSale(Cell cell[]) throws Exception {
	
		String entity = cell[1].getContents().toUpperCase();
		String transactionType = cell[2].getContents();
		Double agreedFx = Double.parseDouble(cell[3].getContents());
		String currency = cell[4].getContents();
		Date instructionDate = new SimpleDateFormat("dd-MMM-yyyy").parse(cell[5].getContents());
		Date settlementDate = getActualSettlementDate(currency, new SimpleDateFormat("dd-MMM-yyyy").parse(cell[6].getContents()));
		Double units = Double.parseDouble(cell[7].getContents());
		Double pricePerUnit = Double.parseDouble(cell[8].getContents());
		
		if("B".equalsIgnoreCase(transactionType)) {
			if(totalOutgoingSettledPerEntity.get(entity) != null) {
				Double currentTotalForEntity = totalOutgoingSettledPerEntity.get(entity);
				Double thisTradeAmount = pricePerUnit * units * agreedFx;
				Double newTotalForEntity = currentTotalForEntity + thisTradeAmount;
				totalOutgoingSettledPerEntity.put(entity,newTotalForEntity);
				
				if(totalOutgoingSettledPerDate.get(settlementDate) != null) {
					Double currentTotalForDate = totalOutgoingSettledPerDate.get(settlementDate);
					Double newTotalForDate = currentTotalForDate + thisTradeAmount;
					totalOutgoingSettledPerDate.put(settlementDate, newTotalForDate);
				}
				else {
					totalOutgoingSettledPerDate.put(settlementDate, thisTradeAmount);
				}
			}
			else {
				Double thisTradeAmount = pricePerUnit * units * agreedFx;
				totalOutgoingSettledPerEntity.put(entity,thisTradeAmount);
				
				if(totalOutgoingSettledPerDate.get(settlementDate) != null) {
					Double currentTotalForDate = totalOutgoingSettledPerDate.get(settlementDate);
					Double newTotalForDate = currentTotalForDate + thisTradeAmount;
					totalOutgoingSettledPerDate.put(settlementDate, newTotalForDate);
				} else {
					totalOutgoingSettledPerDate.put(settlementDate, thisTradeAmount);
				}
			}
		}
		else if("S".equalsIgnoreCase(transactionType)){
			if(totalIncomingSettledPerEntity.get(entity) != null) {
				Double currentTotalForEntity = totalIncomingSettledPerEntity.get(entity);
				Double thisTradeAmount = pricePerUnit * units * agreedFx;
				Double newTotalForEntity = currentTotalForEntity + thisTradeAmount;
				totalIncomingSettledPerEntity.put(entity,newTotalForEntity);
				
				if(totalIncomingSettledPerDate.get(settlementDate) != null) {
					Double currentTotalForDate = totalIncomingSettledPerDate.get(settlementDate);
					Double newTotalForDate = currentTotalForDate + thisTradeAmount;
					totalIncomingSettledPerDate.put(settlementDate, newTotalForDate);
				}
				else {
					totalIncomingSettledPerDate.put(settlementDate, thisTradeAmount);
				}
			}
			else {
				Double thisTradeAmount = pricePerUnit * units * agreedFx;
				totalIncomingSettledPerEntity.put(entity,thisTradeAmount);
				
				if(totalIncomingSettledPerDate.get(settlementDate) != null) {
					Double currentTotalForDate = totalIncomingSettledPerDate.get(settlementDate);
					Double newTotalForDate = currentTotalForDate + thisTradeAmount;
					totalIncomingSettledPerDate.put(settlementDate, newTotalForDate);
				}
				else {
					totalIncomingSettledPerDate.put(settlementDate, thisTradeAmount);
				}
			}
			
		}
		
	}
	
	public static Date getActualSettlementDate(String currency, Date settlementDate) throws Exception {
		
		Calendar c = Calendar.getInstance();
		c.setTime(settlementDate);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		
		if("AED".equals(currency)|| "SAR".equals(currency)) {
			if(dayOfWeek == 6) {
				c.add(Calendar.DATE, 2);
			}else if(dayOfWeek == 7) {
				c.add(Calendar.DATE, 1);
			}
		}
		else {
			if(dayOfWeek == 1) {
				c.add(Calendar.DATE, 1);
			}else if(dayOfWeek == 7) {
				c.add(Calendar.DATE, 2);
			}
		}
		String updatedDate = new SimpleDateFormat("dd-MMM-yyyy").format(c.getTime());
		settlementDate = new SimpleDateFormat("dd-MMM-yyyy").parse(updatedDate);
		
		return settlementDate;
	}
	
	private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap)
    {

        List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Double>>()
        {
            public int compare(Entry<String, Double> o1,
                    Entry<String, Double> o2)
            {
               
               return o2.getValue().compareTo(o1.getValue());

            }
        });

        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Entry<String, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

}
