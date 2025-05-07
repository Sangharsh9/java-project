import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class OwnerDashboard extends JFrame {
    JButton addRoomButton, viewAllBookingsButton, viewRoomsButton;
    JTextArea outputArea;

    public OwnerDashboard() {
        super("Owner Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        // 1) Use BackgroundPanel with our lobby image
        BackgroundPanel bg = new BackgroundPanel("/img.png");
        bg.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setContentPane(bg);

        // 2) Create controls
        addRoomButton = new JButton("Add New Room");
        viewRoomsButton = new JButton("View All Rooms");
        viewAllBookingsButton = new JButton("View All Bookings");

        outputArea = new JTextArea(20, 50);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // 3) Add them onto the background panel
        bg.add(addRoomButton);
        bg.add(viewRoomsButton);
        bg.add(viewAllBookingsButton);
        bg.add(scrollPane);

        // 4) Wire up actions
        addRoomButton.addActionListener(e -> addNewRoom());
        viewRoomsButton.addActionListener(e -> viewAllRooms());
        viewAllBookingsButton.addActionListener(e -> viewAllBookings());

        setVisible(true);
    }

    // ... your existing addNewRoom(), viewAllRooms(), viewAllBookings() methods ...


    private void addNewRoom() {
        JTextField roomNumberField = new JTextField();
        JTextField roomTypeField = new JTextField();
        JTextField priceField = new JTextField();

        Object[] message = {
                "Room Number:", roomNumberField,
                "Room Type:", roomTypeField,
                "Price:", priceField,
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Room", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String roomNumber = roomNumberField.getText();
            String roomType = roomTypeField.getText();
            String priceStr = priceField.getText();

            if (roomNumber.isEmpty() || roomType.isEmpty() || priceStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!");
                return;
            }

            try (Connection conn = DBConnection.getConnection()) {
                String sql = "INSERT INTO rooms (room_number, room_type, price, status) VALUES (?, ?, ?, 'available')";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, roomNumber);
                stmt.setString(2, roomType);
                stmt.setDouble(3, Double.parseDouble(priceStr));
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Room added successfully!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding room!");
            }
        }
    }

    private void viewAllRooms() {
        outputArea.setText("");
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM rooms";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            outputArea.append("All Rooms:\n");
            while (rs.next()) {
                outputArea.append("Room ID: " + rs.getInt("room_id") +
                        ", Number: " + rs.getString("room_number") +
                        ", Type: " + rs.getString("room_type") +
                        ", Price: " + rs.getDouble("price") +
                        ", Status: " + rs.getString("status") + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading rooms.");
        }
    }

    private void viewAllBookings() {
        outputArea.setText("");
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT b.booking_id, u.username, r.room_number, b.check_in_date, b.check_out_date " +
                    "FROM bookings b JOIN users u ON b.user_id = u.user_id " +
                    "JOIN rooms r ON b.room_id = r.room_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            outputArea.append("All Bookings:\n");
            while (rs.next()) {
                outputArea.append("Booking ID: " + rs.getInt("booking_id") +
                        ", Customer: " + rs.getString("username") +
                        ", Room: " + rs.getString("room_number") +
                        ", Check-in: " + rs.getDate("check_in_date") +
                        ", Check-out: " + rs.getDate("check_out_date") + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading bookings.");
        }
    }
}
