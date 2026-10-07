package ui;

import exception.InvalidDeviceException;
import exception.PowerLimitExceededException;
import model.AirConditioner;
import model.Appliance;
import model.Fan;
import model.Light;
import model.PowerLog;
import model.Room;
import service.ApplianceManager;
import simulation.DeviceSimulator;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SmartHomeGUI extends JFrame {

    private final ApplianceManager manager;

    private DefaultTableModel applianceTableModel;
    private JTable            applianceTable;

    private DefaultTableModel roomTableModel;

    private DefaultListModel<String> historyListModel;

    private JTextArea reportArea;

    private JLabel powerStatusLabel;

    public SmartHomeGUI(ApplianceManager manager) {
        super("Smart Home Control Panel");
        this.manager = manager;
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildTabPanel(),    BorderLayout.CENTER);
        add(buildStatusBar(),   BorderLayout.SOUTH);

        new javax.swing.Timer(2000, e -> updateStatusBar()).start();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildTopPanel() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(33, 97, 140));
        JLabel title = new JLabel("  Smart Home Control Panel", JLabel.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        top.add(title, BorderLayout.WEST);

        powerStatusLabel = new JLabel("Power: 0.0 W / 5000.0 W  ", JLabel.RIGHT);
        powerStatusLabel.setForeground(Color.WHITE);
        top.add(powerStatusLabel, BorderLayout.EAST);
        top.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return top;
    }

    private JTabbedPane buildTabPanel() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Appliances",    buildApplianceTab());
        tabs.addTab("Rooms",         buildRoomsTab());
        tabs.addTab("Power History", buildHistoryTab());
        tabs.addTab("Report",        buildReportTab());
        tabs.addTab("Simulate",      buildSimulateTab());
        return tabs;
    }

    private JPanel buildApplianceTab() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));

        String[] cols = {"Device ID", "Name", "Room", "Type", "Status", "Power (W)"};
        applianceTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        applianceTable = new JTable(applianceTableModel);
        panel.add(new JScrollPane(applianceTable), BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn    = new JButton("Add");
        JButton removeBtn = new JButton("Remove");
        JButton onBtn     = new JButton("Turn ON");
        JButton offBtn    = new JButton("Turn OFF");
        JButton renameBtn = new JButton("Rename");
        JButton refreshBtn= new JButton("Refresh");

        btnBar.add(addBtn); btnBar.add(removeBtn);
        btnBar.add(onBtn);  btnBar.add(offBtn);
        btnBar.add(renameBtn); btnBar.add(refreshBtn);
        panel.add(btnBar, BorderLayout.SOUTH);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(12);
        JComboBox<String> searchType = new JComboBox<>(new String[]{"Name","Room","Type","Status"});
        JButton searchBtn = new JButton("Search");
        JButton clearBtn  = new JButton("Show All");
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField); searchBar.add(searchType);
        searchBar.add(searchBtn);   searchBar.add(clearBtn);

        JComboBox<String> sortBox = new JComboBox<>(new String[]{"Sort: Power↓","Sort: Name A-Z","Sort: Status"});
        JButton sortBtn = new JButton("Apply Sort");
        searchBar.add(Box.createHorizontalStrut(20));
        searchBar.add(sortBox); searchBar.add(sortBtn);
        panel.add(searchBar, BorderLayout.NORTH);

        refreshApplianceTable(manager.getAllAppliances());

        refreshBtn.addActionListener(e -> refreshApplianceTable(manager.getAllAppliances()));

        addBtn.addActionListener(e -> showAddDialog());

        removeBtn.addActionListener(e -> {
            String id = getSelectedDeviceId();
            if (id == null) return;
            manager.removeAppliance(id);
            refreshApplianceTable(manager.getAllAppliances());
            refreshRoomTable();
        });

        onBtn.addActionListener(e -> {
            String id = getSelectedDeviceId();
            if (id == null) return;
            try {
                manager.turnOnAppliance(id);
                refreshApplianceTable(manager.getAllAppliances());
                updateStatusBar();
            } catch (PowerLimitExceededException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Power Limit", JOptionPane.WARNING_MESSAGE);
            } catch (InvalidDeviceException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        offBtn.addActionListener(e -> {
            String id = getSelectedDeviceId();
            if (id == null) return;
            manager.turnOffAppliance(id);
            refreshApplianceTable(manager.getAllAppliances());
            updateStatusBar();
        });

        renameBtn.addActionListener(e -> {
            String id = getSelectedDeviceId();
            if (id == null) return;
            String newName = JOptionPane.showInputDialog(this, "New name:", "Rename", JOptionPane.PLAIN_MESSAGE);
            if (newName != null && !newName.trim().isEmpty()) {
                manager.updateApplianceName(id, newName.trim());
                refreshApplianceTable(manager.getAllAppliances());
            }
        });

        searchBtn.addActionListener(e -> {
            String q = searchField.getText().trim();
            if (q.isEmpty()) return;
            String mode = (String) searchType.getSelectedItem();
            List<Appliance> result;
            switch (mode) {
                case "Room":   result = manager.searchByRoom(q);   break;
                case "Type":   result = manager.searchByType(q);   break;
                case "Status": result = manager.searchByStatus("ON".equalsIgnoreCase(q)); break;
                default:       result = manager.searchByName(q);   break;
            }
            refreshApplianceTable(result);
        });

        clearBtn.addActionListener(e -> {
            searchField.setText("");
            refreshApplianceTable(manager.getAllAppliances());
        });

        sortBtn.addActionListener(e -> {
            String sel = (String) sortBox.getSelectedItem();
            List<Appliance> sorted;
            if ("Sort: Name A-Z".equals(sel))       sorted = manager.sortedByName();
            else if ("Sort: Status".equals(sel))    sorted = manager.sortedByStatus();
            else                                     sorted = manager.sortedByPowerDesc();
            refreshApplianceTable(sorted);
        });

        return panel;
    }

    private void refreshApplianceTable(List<Appliance> list) {
        applianceTableModel.setRowCount(0);
        for (Appliance a : list)
            applianceTableModel.addRow(new Object[]{
                a.getDeviceId(), a.getDeviceName(), a.getRoomId(),
                a.getType(), a.getStatus(), a.getPowerConsumption()
            });
    }

    private String getSelectedDeviceId() {
        int row = applianceTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a device.", "No Selection", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return (String) applianceTableModel.getValueAt(row, 0);
    }

    private void showAddDialog() {
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"LIGHT","FAN","AC"});
        JTextField idField    = new JTextField("D00" + (manager.getAllAppliances().size() + 1));
        JTextField nameField  = new JTextField();
        JTextField roomField  = new JTextField();
        JTextField powerField = new JTextField("100");

        JPanel form = new JPanel(new GridLayout(5, 2, 4, 4));
        form.add(new JLabel("Type:"));      form.add(typeBox);
        form.add(new JLabel("Device ID:")); form.add(idField);
        form.add(new JLabel("Name:"));      form.add(nameField);
        form.add(new JLabel("Room ID:"));   form.add(roomField);
        form.add(new JLabel("Power (W):")); form.add(powerField);

        int ok = JOptionPane.showConfirmDialog(this, form, "Add Appliance",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            String t    = (String) typeBox.getSelectedItem();
            String id   = idField.getText().trim();
            String name = nameField.getText().trim();
            String room = roomField.getText().trim();
            double pw   = Double.parseDouble(powerField.getText().trim());

            if (manager.getAllRooms().stream().noneMatch(r -> r.getRoomId().equals(room))) {
                manager.addRoom(new Room(room, "Room " + room));
            }

            Appliance a;
            switch (t) {
                case "FAN": a = new Fan(id, name, room, pw); break;
                case "AC":  a = new AirConditioner(id, name, room, pw); break;
                default:    a = new Light(id, name, room, pw); break;
            }
            manager.addAppliance(a);
            refreshApplianceTable(manager.getAllAppliances());
            refreshRoomTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid power value.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InvalidDeviceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildRoomsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        String[] cols = {"Room ID", "Room Name", "Appliances"};
        roomTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable roomTable = new JTable(roomTableModel);
        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        refreshRoomTable();
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshRoomTable());
        JPanel bot = new JPanel(); bot.add(refreshBtn);
        panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshRoomTable() {
        if (roomTableModel == null) return;
        roomTableModel.setRowCount(0);
        for (Room r : manager.getAllRooms())
            roomTableModel.addRow(new Object[]{
                r.getRoomId(), r.getRoomName(), String.join(", ", r.getApplianceIds())
            });
    }

    private JPanel buildHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        historyListModel = new DefaultListModel<>();
        JList<String> list = new JList<>(historyListModel);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshHistory());
        JPanel bot = new JPanel(); bot.add(refreshBtn);
        panel.add(bot, BorderLayout.SOUTH);
        refreshHistory();
        return panel;
    }

    private void refreshHistory() {
        if (historyListModel == null) return;
        historyListModel.clear();
        for (PowerLog log : manager.getPowerHistory())
            historyListModel.addElement(log.toString());
    }

    private JPanel buildReportTab() {
        JPanel panel = new JPanel(new BorderLayout());
        reportArea = new JTextArea();
        reportArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        reportArea.setEditable(false);
        panel.add(new JScrollPane(reportArea), BorderLayout.CENTER);
        JButton genBtn = new JButton("Generate Report");
        genBtn.addActionListener(e -> {
            reportArea.setText(manager.generateReport());
            refreshHistory();
        });
        JPanel bot = new JPanel(); bot.add(genBtn);
        panel.add(bot, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildSimulateTab() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JComboBox<String> deviceBox = new JComboBox<>();
        for (Appliance a : manager.getAllAppliances())
            deviceBox.addItem(a.getDeviceId() + " - " + a.getDeviceName());

        JSpinner cyclesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        JButton  startBtn      = new JButton("Start Simulation");
        JTextArea  simLog      = new JTextArea(10, 50);
        simLog.setEditable(false);

        form.add(new JLabel("Device:"));   form.add(deviceBox);
        form.add(new JLabel("Cycles:"));   form.add(cyclesSpinner);
        form.add(startBtn);
        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(simLog), BorderLayout.CENTER);

        startBtn.addActionListener(e -> {
            String sel = (String) deviceBox.getSelectedItem();
            if (sel == null) return;
            String deviceId = sel.split(" - ")[0];
            int cycles = (int) cyclesSpinner.getValue();
            try {
                Appliance a = manager.getAppliance(deviceId);
                DeviceSimulator sim = new DeviceSimulator(a, cycles, manager);
                Thread t = new Thread(sim);
                t.setDaemon(true);
                t.start();
                simLog.append("Started simulation for " + a.getDeviceName() + " (" + cycles + " cycles)\n");
            } catch (InvalidDeviceException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    private JLabel buildStatusBar() {
        JLabel bar = new JLabel("  Ready");
        bar.setBorder(BorderFactory.createEtchedBorder());
        return bar;
    }

    private void updateStatusBar() {
        double used = manager.getCurrentPowerUsage();
        powerStatusLabel.setText(String.format("Power: %.1f W / 5000.0 W  ", used));
    }
}
