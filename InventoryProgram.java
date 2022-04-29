package inventoryPackage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class InventoryProgram {

	public static void main(String[] args) {

		try {
			String host = "jdbc:mysql://localhost:3306/inventory_database";
			String username = "root";
			String password = "thesequal";
			// Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection(host, username, password);
			//////////////////////////////////////////////////////////////////////////

			boolean run = true;

			while (run) {

				printCommands();
				Scanner scan = new Scanner(System.in);
				String Uresponse = scan.nextLine();

//			Scan items into database
				if (Uresponse.equalsIgnoreCase("start scanning")) {
					scan(conn);

				}
//			Search for items by barcode
				if (Uresponse.equalsIgnoreCase("item search")) {
					Scanner barcodeIn = new Scanner(System.in);
					long barcode = barcodeIn.nextLong();
					itemSearch(conn, barcode);
				}
//			print total inventory
				if (Uresponse.equalsIgnoreCase("inventory total")) {
					inventoryTotal(conn);
				}
//			List specific category total
				if (Uresponse.equalsIgnoreCase("List specific category total")) {
					System.out.println("what category would you like to see?  ");
					Scanner catIn = new Scanner(System.in);
					String category = catIn.nextLine();
					categoryTotal(conn, category);

				}

//			Add, delete, or change item
				if (Uresponse.equals("add item")) {
					System.out.println(
							"type in name, cost, barcode, and item category for the item you would like to add.");
					Scanner field = new Scanner(System.in);
					String name = field.nextLine();
					double cost = field.nextDouble();
					long barcode = field.nextLong();
					String itemCat = field.next();
					addItem(conn, name, cost, barcode, itemCat);
				}
				if (Uresponse.equals("delete item")) {
					deleteItem(conn);
				}
				if (Uresponse.equals("change item")) {
					itemChange(conn);
				}

//			quit
				if (Uresponse.equalsIgnoreCase("Quit")) {
					run = false;
				}

			}

		} catch (SQLException err) {
			System.out.println(err.getMessage());
		}

	}

	private static void printCommands() {
		System.out.println("start scanning");
		System.out.println("item search");
		System.out.println("inventory total");
		System.out.println("List specific category total");
		System.out.println("add, delete, or change item");
		System.out.println("Quit");
		System.out.print("> ");
	}

	// barcode scanner method
	private static void scan(Connection conn) {
		try {
			Scanner bScanner = new Scanner(System.in);
			String barcode = bScanner.nextLine();
			long barcodeNum = 0;
			if (barcode.equals("stop")) {
				return;
			} else {
				barcodeNum = Long.parseLong(barcode);
				String scanQ = "UPDATE item SET amount_on_hand = amount_on_hand + 1 WHERE barcode = ?;";
				PreparedStatement scan = conn.prepareStatement(scanQ);
				scan.setLong(1, barcodeNum);
				scan.executeUpdate();
				// check if item is not in database
				String updateCheckQ = "SELECT barcode FROM item WHERE barcode = ?";
				PreparedStatement updateCheck = conn.prepareStatement(updateCheckQ,ResultSet.TYPE_SCROLL_SENSITIVE, 
                        ResultSet.CONCUR_UPDATABLE);
				updateCheck.setLong(1, barcodeNum);
				ResultSet rs = updateCheck.executeQuery();
				while (rs.next()) {
					if (rs.getLong("barcode") == barcodeNum) {
						scan(conn);
					}
				}
				if (!rs.previous() ) {    
				    System.out.println("item is not in system. please add in new item \n"); 
				} 
				
			}
			
		} catch (SQLException err) {
			System.out.println(err.getMessage());
		}
	}

	// item search method
	private static void itemSearch(Connection conn, Long barcode) {
		try {
			String itemSearchQ = "SELECT * FROM item where barcode = ?;";
			PreparedStatement itemSearch = conn.prepareStatement(itemSearchQ);
			itemSearch.setLong(1, barcode);
			ResultSet foundItem = itemSearch.executeQuery();
			while (foundItem.next()) {
				System.out.println(foundItem.getString("item_name"));
				System.out.println("cost: $" + foundItem.getDouble("cost"));
				System.out.println("barcode : " + foundItem.getLong("barcode"));
				System.out.println("amount on hand: " + foundItem.getInt("amount_on_hand"));
				System.out.println("category: " + foundItem.getString("item_category") + "\n");
			}
		} catch (SQLException err) {
			System.out.println(err.getMessage());
		}

	}

	// print inventory total method
	private static void inventoryTotal(Connection conn) {
		try {
			String totalInvQ = "SELECT SUM(cost * amount_on_hand) FROM item;";
			PreparedStatement totalInv = conn.prepareStatement(totalInvQ);
			ResultSet result = totalInv.executeQuery();
			while (result.next()) {
				System.out.println("Total inventory is: $" + result.getDouble("SUM(cost * amount_on_hand)") + "\n");
			}

		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	// List specific category total
	private static void categoryTotal(Connection conn, String category) {
		// find category
		String cat = "";
		if (category.equals("beer")) {
			cat = "beer";
		}
		if (category.equals("wine")) {
			cat = "wine";
		}
		if (category.equals("grocery")) {
			cat = "grocery";
		}
		if (category.equals("meat")) {
			cat = "meat";
		}
		try {
			String catInvQ = "SELECT SUM(cost * amount_on_hand) FROM item WHERE item_category = ?;";
			PreparedStatement catInv = conn.prepareStatement(catInvQ);
			catInv.setString(1, cat);
			ResultSet result = catInv.executeQuery();
			while (result.next()) {
				System.out.println(cat + " inventory is: $" + result.getDouble("SUM(cost * amount_on_hand)") + "\n");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}

	// add item
	private static void addItem(Connection conn, String name, Double cost, Long barcode, String itemCat) {
		try {

			String addItemQ = "INSERT INTO item VALUES(?,?,?,0,?);";
			PreparedStatement addItem = conn.prepareStatement(addItemQ);
			addItem.setString(1, name);
			addItem.setDouble(2, cost);
			addItem.setLong(3, barcode);
			addItem.setString(4, itemCat);
			addItem.executeUpdate();
			System.out.println("");
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}

	// delete item
	private static void deleteItem(Connection conn) {
		try {
			System.out.println("please type in the barcode of the item you would like to delete");
			Scanner barcodeIn = new Scanner(System.in);
			Long barcode = barcodeIn.nextLong();
			String deleteQ = "DELETE FROM item WHERE barcode = ?;";
			PreparedStatement deleteItem = conn.prepareStatement(deleteQ);
			deleteItem.setLong(1, barcode);
			deleteItem.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	// change name
	private static void changeName(Connection conn, String name, Long barcode) {
		try {
			String nameChangeQ = "UPDATE item SET item_name = ? WHERE barcode = ?";
			PreparedStatement nameChange = conn.prepareStatement(nameChangeQ);
			nameChange.setString(1, name);
			nameChange.setLong(2, barcode);
			nameChange.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}

	// change cost
	private static void changeCost(Connection conn, Double cost, Long barcode) {
		try {
			String costChangeQ = "UPDATE item SET cost = ? WHERE barcode = ?";
			PreparedStatement costChange = conn.prepareStatement(costChangeQ);
			costChange.setDouble(1, cost);
			costChange.setLong(2, barcode);
			costChange.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}

	// change amount on hand
	private static void changeAOH(Connection conn, int AOH, Long barcode) {
		try {
			String AOHChangeQ = "UPDATE item SET amount_on_hand = ? WHERE barcode = ?";
			PreparedStatement AOHChange = conn.prepareStatement(AOHChangeQ);
			AOHChange.setInt(1, AOH);
			AOHChange.setLong(2, barcode);
			AOHChange.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}

	}

	// change item category
	private static void changeCat(Connection conn, String cat, Long barcode) {
		try {
			String catChangeQ = "UPDATE item SET item_cat = ? WHERE barcode = ?";
			PreparedStatement catChange = conn.prepareStatement(catChangeQ);
			catChange.setString(1, cat);
			catChange.setLong(2, barcode);
			catChange.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	// edit item
	private static void itemChange(Connection conn) {
		System.out.println("please enter barcode of item you would like to change");
		Scanner scanner = new Scanner(System.in);
		long barcode = scanner.nextLong();
		System.out.println("would you like to change the name, cost, AOH, or category?");
		String command = scanner.next();
		// change name
		if (command.equals("name")) {
			System.out.println("please enter the new name for the item");
			String newName = scanner.next();
			changeName(conn, newName, barcode);
		}
		// change cost
		if (command.equals("cost")) {
			System.out.println("please enter the new cost for the item");
			Double newCost = scanner.nextDouble();
			changeCost(conn, newCost, barcode);
		}
		// change amount on hand
		if (command.equals("AOH")) {
			System.out.println("please enter the new amount on hand for the item");
			int newAOH = scanner.nextInt();
			changeAOH(conn, newAOH, barcode);
		}
		// change item category
		if (command.equals("category")) {
			System.out.println("please enter the new category for the item");
			String newcat = scanner.next();
			changeCat(conn, newcat, barcode);
		}

	}

}
