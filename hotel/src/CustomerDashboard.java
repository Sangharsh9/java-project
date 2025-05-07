import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CustomerDashboard extends JFrame {
    JButton viewRoomsButton, bookRoomButton, myBookingsButton, viewBillButton;
    JTextArea outputArea;
    int currentUserId;

    public CustomerDashboard(int userId) {
        this.currentUserId = userId;

        setTitle("Customer Dashboard");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 1) Use BackgroundPanel with our lobby image
        BackgroundPanel bg = new BackgroundPanel("/img.png");
        bg.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setContentPane(bg);

        setLayout(new FlowLayout());

        viewRoomsButton = new JButton("View Available Rooms");
        bookRoomButton = new JButton("Book a Room");
        myBookingsButton = new JButton("View My Bookings");
        viewBillButton = new JButton("View Total Bill"); // New button

        outputArea = new JTextArea(15, 50);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        add(viewRoomsButton);
        add(bookRoomButton);
        add(myBookingsButton);
        add(viewBillButton); // Add bill button
        add(scrollPane);

        viewRoomsButton.addActionListener(e -> viewAvailableRooms());
        bookRoomButton.addActionListener(e -> bookRoom());
        myBookingsButton.addActionListener(e -> viewMyBookings());
        viewBillButton.addActionListener(e -> viewTotalBill());

        setVisible(true);
    }

    private void viewAvailableRooms() {
        outputArea.setText("");
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM rooms WHERE status = 'available'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            outputArea.append("Available Rooms:\n");
            while (rs.next()) {
                outputArea.append("Room ID: " + rs.getInt("room_id") +
                        ", Number: " + rs.getString("room_number") +
                        ", Type: " + rs.getString("room_type") +
                        ", Price: ₹" + rs.getDouble("price") + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading rooms.");
        }
    }

    private void bookRoom() {
        String roomIdStr = JOptionPane.showInputDialog(this, "Enter Room ID to Book:");
        String checkIn = JOptionPane.showInputDialog(this, "Enter Check-In Date (YYYY-MM-DD):");
        String checkOut = JOptionPane.showInputDialog(this, "Enter Check-Out Date (YYYY-MM-DD):");

        if (roomIdStr == null || checkIn == null || checkOut == null) return;

        try (Connection conn = DBConnection.getConnection()) {
            int roomId = Integer.parseInt(roomIdStr);

            // Check availability
            String checkSql = "SELECT * FROM rooms WHERE room_id=? AND status='available'";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, roomId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Book the room
                String bookSql = "INSERT INTO bookings (user_id, room_id, check_in_date, check_out_date) VALUES (?, ?, ?, ?)";
                PreparedStatement bookStmt = conn.prepareStatement(bookSql);
                bookStmt.setInt(1, currentUserId);
                bookStmt.setInt(2, roomId);
                bookStmt.setString(3, checkIn);
                bookStmt.setString(4, checkOut);
                bookStmt.executeUpdate();

                // Update room status
                String updateSql = "UPDATE rooms SET status='booked' WHERE room_id=?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, roomId);
                updateStmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "Room booked successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Room not available.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error booking room.");
        }
    }

    private void viewMyBookings() {
        outputArea.setText("");
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT b.booking_id, r.room_number, b.check_in_date, b.check_out_date " +
                    "FROM bookings b JOIN rooms r ON b.room_id = r.room_id " +
                    "WHERE b.user_id=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            outputArea.append("My Bookings:\n");
            while (rs.next()) {
                outputArea.append("Booking ID: " + rs.getInt("booking_id") +
                        ", Room: " + rs.getString("room_number") +
                        ", Check-in: " + rs.getDate("check_in_date") +
                        ", Check-out: " + rs.getDate("check_out_date") + "\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading your bookings.");
        }
    }

    private void viewTotalBill() {
        outputArea.setText("");
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT r.price, DATEDIFF(b.check_out_date, b.check_in_date) AS days " +
                    "FROM bookings b JOIN rooms r ON b.room_id = r.room_id " +
                    "WHERE b.user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, currentUserId);
            ResultSet rs = stmt.executeQuery();

            double total = 0;
            while (rs.next()) {
                double pricePerDay = rs.getDouble("price");
                int days = rs.getInt("days");
                total += pricePerDay * Math.max(days, 1); // Ensure minimum 1 day
            }

            outputArea.append("Your Total Bill: ₹" + total);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error calculating bill.");
        }
    }
}
