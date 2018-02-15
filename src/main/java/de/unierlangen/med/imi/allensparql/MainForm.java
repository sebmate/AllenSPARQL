/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.unierlangen.med.imi.allensparql;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import javax.swing.text.DefaultCaret;
import java.util.*;
import java.io.*;
import java.util.concurrent.TimeUnit;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.OpAsQuery;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.resultset.RDFOutput;

/**
 *
 * @author matesn
 */
public class MainForm extends javax.swing.JFrame {

    private static boolean NoSPARQL;

    public static void deleteAllRows(final DefaultTableModel model) {
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            model.removeRow(i);
        }
    }
    private boolean NoSQL;

    private void handleSparqlNotReady() {
        NoSPARQL = true;
        uploadData.setEnabled(false);
        uploadData.setSelected(false);
        simpleAgg.setEnabled(false);
        complexAgg.setEnabled(false);
        noAgg.setSelected(true);
        inferAllen.setEnabled(false);
        createSimplified.setEnabled(false);
        runETL.setEnabled(false);
        JOptionPane.showMessageDialog(null, "The SPARQL endpoint (Fuseki) is not accessible.\nYou can not fully use this program while this problem persists.", "SPARQL Endpoint Problem", JOptionPane.ERROR_MESSAGE);
    }

    private boolean refreshInProgress;

    /**
     * Creates new form MainForm
     */
    public MainForm() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        initComponents();

        buttonGroup1.add(complexAgg);
        buttonGroup1.add(simpleAgg);
        buttonGroup1.add(noAgg);

        // https://coderanch.com/t/346106/java/Graying-entire-table
        configTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable tab, Object val, boolean isSelected, boolean hasFocus, int row, int col) {
                setEnabled(tab.isEnabled());
                return super.getTableCellRendererComponent(tab, val, isSelected, hasFocus, row, col);
            }
        });

        // Based on http://www.rgagnon.com/javadetails/java-0490.html
        TimerTask task = new FileWatcher(new File("TemporalLogic.txt")) {
            protected void onChange(File file) {
                System.out.println("Detected change in TemporalLogic.txt, generating new SPARQL query ...");
                executeAllen();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, new Date(), 1000);

        Thread init = new Thread() {
            public void run() {
                try {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    Dimension frameSize = LoadingInfo.getSize();
                    if (frameSize.height > screenSize.height) {
                        frameSize.height = screenSize.height;
                    }
                    if (frameSize.width > screenSize.width) {
                        frameSize.width = screenSize.width;
                    }
                    LoadingInfo.pack();
                    LoadingInfo.setLocation((screenSize.width - frameSize.width) / 2,
                            (screenSize.height - frameSize.height) / 2);
                    LoadingInfo.setVisible(true);
                    LoadingInfo.toFront();
                    refreshInProgress = true;
                    refreshQueries();
                    refreshInProgress = false;
                    DefaultTableModel model = (DefaultTableModel) configTable.getModel();
                    deleteAllRows(model);
                    refreshConfigTable();
                    updateTripleCount();
                    LoadingInfo.setVisible(false);
                } catch (Exception v) {
                    System.out.println("Lalala");
                    v.printStackTrace();
                }
            }
        };
        init.start();
    }

    void executeAllen() {
        autoGenSPARQL();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LoadingInfo = new javax.swing.JFrame();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        buttonGroup1 = new javax.swing.ButtonGroup();
        previousQueries = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        refreshQueries = new javax.swing.JButton();
        createView = new javax.swing.JCheckBox();
        uploadData = new javax.swing.JCheckBox();
        linkIntervals = new javax.swing.JCheckBox();
        createSuperIntervals = new javax.swing.JCheckBox();
        inferAllen = new javax.swing.JCheckBox();
        allButton = new javax.swing.JButton();
        noneButton = new javax.swing.JButton();
        runETL = new javax.swing.JButton();
        tripleCount = new javax.swing.JLabel();
        patientCount = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        logWindow = new javax.swing.JTextArea();
        autoRunSPARQL = new javax.swing.JCheckBox();
        runSPARQL = new javax.swing.JButton();
        linkSubintervals = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        configTable = new javax.swing.JTable();
        jLabel7 = new javax.swing.JLabel();
        addRow = new javax.swing.JButton();
        removeRow = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        noAgg = new javax.swing.JRadioButton();
        simpleAgg = new javax.swing.JRadioButton();
        complexAgg = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        uncertaintyThreshold = new javax.swing.JTextField();
        linkThreshold = new javax.swing.JTextField();
        UncThrsLabel = new javax.swing.JLabel();
        aggThrsLabel = new javax.swing.JLabel();
        duplicateIntervals = new javax.swing.JCheckBox();
        createSimplified = new javax.swing.JCheckBox();
        querySimplified = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        SPARQLcode = new javax.swing.JTextArea();
        TranslatorStatus = new javax.swing.JLabel();
        explainPlan = new javax.swing.JButton();

        LoadingInfo.setBackground(new java.awt.Color(153, 153, 153));
        LoadingInfo.setForeground(java.awt.Color.gray);
        LoadingInfo.setMinimumSize(new java.awt.Dimension(335, 62));
        LoadingInfo.setUndecorated(true);

        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Logo.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout LoadingInfoLayout = new javax.swing.GroupLayout(LoadingInfo.getContentPane());
        LoadingInfo.getContentPane().setLayout(LoadingInfoLayout);
        LoadingInfoLayout.setHorizontalGroup(
            LoadingInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        LoadingInfoLayout.setVerticalGroup(
            LoadingInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("AllenSPARQL: Temporal Phenotyping with SPARQL");
        setLocationByPlatform(true);

        previousQueries.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        previousQueries.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                previousQueriesItemStateChanged(evt);
            }
        });
        previousQueries.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                previousQueriesMouseClicked(evt);
            }
        });
        previousQueries.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                previousQueriesCaretPositionChanged(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
            }
        });
        previousQueries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousQueriesActionPerformed(evt);
            }
        });

        jLabel1.setText(" i2b2 query:");

        refreshQueries.setText("Refresh");
        refreshQueries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshQueriesActionPerformed(evt);
            }
        });

        createView.setBackground(java.awt.Color.lightGray);
        createView.setSelected(true);
        createView.setText("Create view with the query's data for D2RQ");

        uploadData.setBackground(java.awt.Color.lightGray);
        uploadData.setSelected(true);
        uploadData.setText("Upload these data to Fuseki via D2RQ");

        linkIntervals.setBackground(java.awt.Color.lightGray);
        linkIntervals.setText("Link near intervals");
        linkIntervals.setEnabled(false);

        createSuperIntervals.setBackground(java.awt.Color.lightGray);
        createSuperIntervals.setText("Aggregate near intervals into episodes");
        createSuperIntervals.setEnabled(false);

        inferAllen.setBackground(java.awt.Color.lightGray);
        inferAllen.setSelected(true);
        inferAllen.setText("Create Allen relations in source data");

        allButton.setText("Select all");
        allButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                allButtonActionPerformed(evt);
            }
        });

        noneButton.setText("Select none");
        noneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noneButtonActionPerformed(evt);
            }
        });

        runETL.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        runETL.setForeground(new java.awt.Color(255, 0, 0));
        runETL.setText("Run ETL");
        runETL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runETLActionPerformed(evt);
            }
        });

        tripleCount.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        tripleCount.setText("Triples: unknown            ");

        patientCount.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        patientCount.setText("Identified patients: unknown");

        logWindow.setColumns(20);
        logWindow.setLineWrap(true);
        logWindow.setRows(5);
        jScrollPane1.setViewportView(logWindow);

        autoRunSPARQL.setText("Automatically run SPARQL queries");

        runSPARQL.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        runSPARQL.setForeground(new java.awt.Color(255, 0, 0));
        runSPARQL.setText("Run SPARQL query");
        runSPARQL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runSPARQLActionPerformed(evt);
            }
        });

        linkSubintervals.setBackground(java.awt.Color.lightGray);
        linkSubintervals.setText("Link episodes to intervals, count subintervals, compute average values");
        linkSubintervals.setEnabled(false);

        jPanel1.setBackground(java.awt.Color.lightGray);

        configTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Original name", "New name", "Aggregation threshold (s)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        configTable.setEnabled(false);
        configTable.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                configTableKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(configTable);

        jLabel7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel7.setText("Temporal aggregation and uncertainty handling");

        addRow.setText("+");
        addRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRowActionPerformed(evt);
            }
        });

        removeRow.setText("-");
        removeRow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeRowActionPerformed(evt);
            }
        });

        jPanel3.setBackground(java.awt.Color.lightGray);

        noAgg.setBackground(java.awt.Color.lightGray);
        noAgg.setSelected(true);
        noAgg.setText("No aggregation");
        noAgg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noAggActionPerformed(evt);
            }
        });

        simpleAgg.setBackground(java.awt.Color.lightGray);
        simpleAgg.setText("Simple aggregation");
        simpleAgg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simpleAggActionPerformed(evt);
            }
        });

        complexAgg.setBackground(java.awt.Color.lightGray);
        complexAgg.setText("Complex aggregation");
        complexAgg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                complexAggActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(noAgg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(complexAgg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(simpleAgg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(noAgg)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(simpleAgg)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(complexAgg)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBackground(java.awt.Color.lightGray);

        uncertaintyThreshold.setText("60*5");

        linkThreshold.setText("60*60*24");
        linkThreshold.setEnabled(false);
        linkThreshold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkThresholdActionPerformed(evt);
            }
        });

        UncThrsLabel.setBackground(java.awt.Color.lightGray);
        UncThrsLabel.setText("Uncertainty threshold (s):");

        aggThrsLabel.setBackground(java.awt.Color.lightGray);
        aggThrsLabel.setText("Aggregation threshold (s):");
        aggThrsLabel.setEnabled(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(aggThrsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(linkThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(UncThrsLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(uncertaintyThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(aggThrsLabel)
                    .addComponent(linkThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(UncThrsLabel)
                    .addComponent(uncertaintyThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(addRow)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeRow)))
                        .addContainerGap())))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addRow, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeRow, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        duplicateIntervals.setBackground(java.awt.Color.lightGray);
        duplicateIntervals.setText("Duplicate renamed intervals");
        duplicateIntervals.setEnabled(false);

        createSimplified.setBackground(java.awt.Color.lightGray);
        createSimplified.setText("Add simplified Allen relations to source data");

        querySimplified.setText("Query with simplified relations");

        SPARQLcode.setColumns(20);
        SPARQLcode.setRows(5);
        SPARQLcode.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                SPARQLcodePropertyChange(evt);
            }
        });
        SPARQLcode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                SPARQLcodeKeyPressed(evt);
            }
        });
        jScrollPane3.setViewportView(SPARQLcode);

        TranslatorStatus.setBackground(java.awt.Color.lightGray);
        TranslatorStatus.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        TranslatorStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        TranslatorStatus.setText("READY");
        TranslatorStatus.setOpaque(true);

        explainPlan.setText("Show execution plan");
        explainPlan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                explainPlanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(previousQueries, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(refreshQueries))
                            .addComponent(inferAllen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(uploadData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(createView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(duplicateIntervals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(linkIntervals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(createSuperIntervals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(linkSubintervals, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(createSimplified, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(allButton, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(noneButton, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(runETL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tripleCount)
                                .addGap(18, 18, 18)
                                .addComponent(patientCount))
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(autoRunSPARQL)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(querySimplified)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(explainPlan)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(runSPARQL, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(TranslatorStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jScrollPane3)
                        .addComponent(jScrollPane1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(refreshQueries)
                            .addComponent(previousQueries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(createView)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(uploadData)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(duplicateIntervals)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(linkIntervals)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(createSuperIntervals)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(linkSubintervals)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(inferAllen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(createSimplified))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(allButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(noneButton)
                    .addComponent(runETL)
                    .addComponent(tripleCount)
                    .addComponent(patientCount))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 215, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoRunSPARQL)
                    .addComponent(runSPARQL)
                    .addComponent(querySimplified)
                    .addComponent(TranslatorStatus)
                    .addComponent(explainPlan))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void previousQueriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousQueriesActionPerformed
        if (!refreshInProgress) {
            refreshConfigTable();
        }
    }//GEN-LAST:event_previousQueriesActionPerformed

    private void refreshQueriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshQueriesActionPerformed
        refreshInProgress = true;
        refreshQueries();
        refreshInProgress = false;
        refreshConfigTable();
    }//GEN-LAST:event_refreshQueriesActionPerformed

    private void noneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noneButtonActionPerformed
        createView.setSelected(false);
        uploadData.setSelected(false);
        linkIntervals.setSelected(false);
        createSuperIntervals.setSelected(false);
        inferAllen.setSelected(false);
        linkSubintervals.setSelected(false);
    }//GEN-LAST:event_noneButtonActionPerformed

    private void allButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_allButtonActionPerformed
        createView.setSelected(true);
        uploadData.setSelected(true);
        linkIntervals.setSelected(true);
        createSuperIntervals.setSelected(true);
        inferAllen.setSelected(true);
        linkSubintervals.setSelected(true);
    }//GEN-LAST:event_allButtonActionPerformed


    private void runETLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runETLActionPerformed

        new Thread(new Runnable() {
            public void run() {
                executeOperations();
            }
        }).start();

    }//GEN-LAST:event_runETLActionPerformed

    private void runSPARQLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runSPARQLActionPerformed

        new Thread(new Runnable() {
            public void run() {
                runTheSPARQLQuery();
            }
        }).start();
    }//GEN-LAST:event_runSPARQLActionPerformed

    private void previousQueriesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_previousQueriesItemStateChanged

    }//GEN-LAST:event_previousQueriesItemStateChanged

    private void previousQueriesCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_previousQueriesCaretPositionChanged
    }//GEN-LAST:event_previousQueriesCaretPositionChanged

    private void previousQueriesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_previousQueriesMouseClicked

    }//GEN-LAST:event_previousQueriesMouseClicked

    private void complexAggActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_complexAggActionPerformed

        //simpleAgg.setSelected(false);
        linkThreshold.setEnabled(false);
        aggThrsLabel.setEnabled(false);
        configTable.setEnabled(true);
        duplicateIntervals.setSelected(true);
        duplicateIntervals.setEnabled(true);
        createSuperIntervals.setEnabled(true);
        linkIntervals.setEnabled(true);
        linkSubintervals.setEnabled(true);
        createSuperIntervals.setSelected(true);
        linkSubintervals.setSelected(true);
        linkIntervals.setSelected(true);
        testConfigTable();

        new Thread(new Runnable() {
            public void run() {
                for (int a = 0; a < 3; a++) {
                    configTable.setBackground(Color.orange);
                    try {
                        TimeUnit.MILLISECONDS.sleep(75);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    configTable.setBackground(Color.white);
                    try {
                        TimeUnit.MILLISECONDS.sleep(75);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();

    }//GEN-LAST:event_complexAggActionPerformed

    private void simpleAggActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simpleAggActionPerformed
        //complexAgg.setSelected(false);
        linkThreshold.setEnabled(true);
        aggThrsLabel.setEnabled(true);
        configTable.setEnabled(false);
        duplicateIntervals.setSelected(false);
        duplicateIntervals.setEnabled(false);
        createSuperIntervals.setEnabled(true);
        linkIntervals.setEnabled(true);
        linkSubintervals.setEnabled(true);
        createSuperIntervals.setSelected(true);
        linkSubintervals.setSelected(true);
        linkIntervals.setSelected(true);

        new Thread(new Runnable() {
            public void run() {
                for (int a = 0; a < 3; a++) {
                    linkThreshold.setBackground(Color.orange);
                    try {
                        TimeUnit.MILLISECONDS.sleep(75);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    linkThreshold.setBackground(Color.white);
                    try {
                        TimeUnit.MILLISECONDS.sleep(75);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();

    }//GEN-LAST:event_simpleAggActionPerformed

    private void removeRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeRowActionPerformed
        DefaultTableModel model = (DefaultTableModel) configTable.getModel();
        model.removeRow(model.getRowCount() - 1);
    }//GEN-LAST:event_removeRowActionPerformed

    private void addRowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRowActionPerformed
        String data1 = "A";
        String data2 = "A Agg";
        String data3 = "60*60*24";
        Object[] row = {data1, data2, data3};
        DefaultTableModel model = (DefaultTableModel) configTable.getModel();
        model.addRow(row);
    }//GEN-LAST:event_addRowActionPerformed

    private void linkThresholdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkThresholdActionPerformed

    }//GEN-LAST:event_linkThresholdActionPerformed

    private void configTableKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_configTableKeyReleased
        testConfigTable();
    }//GEN-LAST:event_configTableKeyReleased

    private void noAggActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noAggActionPerformed
        linkThreshold.setEnabled(false);
        aggThrsLabel.setEnabled(false);
        configTable.setEnabled(false);
        duplicateIntervals.setSelected(false);
        duplicateIntervals.setEnabled(false);
        createSuperIntervals.setEnabled(false);
        linkIntervals.setEnabled(false);
        linkSubintervals.setEnabled(false);
        createSuperIntervals.setSelected(false);
        linkSubintervals.setSelected(false);
        linkIntervals.setSelected(false);
    }//GEN-LAST:event_noAggActionPerformed

    private void SPARQLcodePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_SPARQLcodePropertyChange

    }//GEN-LAST:event_SPARQLcodePropertyChange

    private void SPARQLcodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_SPARQLcodeKeyPressed
        patientCount.setText("Patients: unknown");
    }//GEN-LAST:event_SPARQLcodeKeyPressed

    private void explainPlanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_explainPlanActionPerformed
        String optimized = explainPlan(SPARQLcode.getText());
        //SPARQLcode.setText(optimized);
    }//GEN-LAST:event_explainPlanActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFrame LoadingInfo;
    private javax.swing.JTextArea SPARQLcode;
    private javax.swing.JLabel TranslatorStatus;
    private javax.swing.JLabel UncThrsLabel;
    private javax.swing.JButton addRow;
    private javax.swing.JLabel aggThrsLabel;
    private javax.swing.JButton allButton;
    private javax.swing.JCheckBox autoRunSPARQL;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton complexAgg;
    private javax.swing.JTable configTable;
    private javax.swing.JCheckBox createSimplified;
    private javax.swing.JCheckBox createSuperIntervals;
    private javax.swing.JCheckBox createView;
    private javax.swing.JCheckBox duplicateIntervals;
    private javax.swing.JButton explainPlan;
    private javax.swing.JCheckBox inferAllen;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JCheckBox linkIntervals;
    private javax.swing.JCheckBox linkSubintervals;
    private javax.swing.JTextField linkThreshold;
    private javax.swing.JTextArea logWindow;
    private javax.swing.JRadioButton noAgg;
    private javax.swing.JButton noneButton;
    private javax.swing.JLabel patientCount;
    private javax.swing.JComboBox previousQueries;
    private javax.swing.JCheckBox querySimplified;
    private javax.swing.JButton refreshQueries;
    private javax.swing.JButton removeRow;
    private javax.swing.JButton runETL;
    private javax.swing.JButton runSPARQL;
    private javax.swing.JRadioButton simpleAgg;
    private javax.swing.JLabel tripleCount;
    private javax.swing.JTextField uncertaintyThreshold;
    private javax.swing.JCheckBox uploadData;
    // End of variables declaration//GEN-END:variables

    private void refreshQueries() {
        try {
            dbHelper helper = new dbHelper();
            helper.loadConfigFile("DBConnection.properties");
            helper.initConnention();
            helper.executeSQL("SELECT QUERY_MASTER_ID, NAME FROM QT_QUERY_MASTER ORDER BY QUERY_MASTER_ID DESC");
            previousQueries.removeAllItems();
            int limit = 0;
            while (helper.nextEntry() && limit <= 20) {
                limit++;
                previousQueries.addItem(helper.getColumn("NAME"));
            }
            helper.closeConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Could not connect to the i2b2 database. Please check the file DBConnection.properties!", "DB Connection Error", JOptionPane.INFORMATION_MESSAGE);
            NoSQL = true;
            LoadingInfo.setVisible(false);
            createView.setEnabled(false);
            createView.setSelected(false);
            uploadData.setEnabled(false);
            uploadData.setSelected(false);
        }
    }

    private void log(String text) {
        String currentText = logWindow.getText();
        currentText += text + "\n";
        logWindow.setText(currentText);
        DefaultCaret caret = (DefaultCaret) logWindow.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }

    private void executeOperations() {
        createView.setBackground(java.awt.Color.lightGray);
        uploadData.setBackground(java.awt.Color.lightGray);
        linkIntervals.setBackground(java.awt.Color.lightGray);
        createSuperIntervals.setBackground(java.awt.Color.lightGray);
        linkSubintervals.setBackground(java.awt.Color.lightGray);
        inferAllen.setBackground(java.awt.Color.lightGray);
        duplicateIntervals.setBackground(Color.lightGray);
        createSimplified.setBackground(Color.lightGray);

        if (createView.isSelected()) {
            createView.setBackground(Color.orange);
            runCreateView();
            createView.setBackground(Color.green);
        }
        if (uploadData.isSelected()) {
            uploadData.setBackground(Color.orange);
            runUploadData();
            uploadData.setBackground(Color.green);
        }
        if (duplicateIntervals.isSelected()) {
            duplicateIntervals.setBackground(Color.orange);
            runDuplicateIntervals();
            duplicateIntervals.setBackground(Color.green);
        }
        if (linkIntervals.isSelected()) {
            linkIntervals.setBackground(Color.orange);
            runLinkIntervals();
            linkIntervals.setBackground(Color.green);
        }
        if (createSuperIntervals.isSelected()) {
            createSuperIntervals.setBackground(Color.orange);
            runCreateSuperIntervals();
            createSuperIntervals.setBackground(Color.green);
        }
        if (linkSubintervals.isSelected()) {
            linkSubintervals.setBackground(Color.orange);
            runLinkSubintervals();
            linkSubintervals.setBackground(Color.green);
        }
        if (inferAllen.isSelected()) {
            inferAllen.setBackground(Color.orange);
            runInferAllen();
            inferAllen.setBackground(Color.green);
        }

        if (createSimplified.isSelected()) {
            createSimplified.setBackground(Color.orange);
            runCreateSimplified();
            createSimplified.setBackground(Color.green);
        }

    }

    private void runCreateView() {
        dbHelper dbHelper = new dbHelper();
        dbHelper.loadConfigFile("DBConnection.properties");
        dbHelper.initConnention();
        dbHelper.executeSQL("SELECT NAME, REQUEST_XML, GENERATED_SQL FROM QT_QUERY_MASTER WHERE NAME = '" + previousQueries.getSelectedItem() + "'");
        dbHelper.nextEntry();

        i2b2Helper helper2 = new i2b2Helper();
        helper2.processI2b2Query(dbHelper.getColumn("REQUEST_XML"));

        List<String> i2b2Vars = helper2.getI2b2Variables();
        List<String> sqlStatements = helper2.getSqlStatements();

        dbHelper.executeSQLDirect("DROP VIEW OBSERVATION_FACT_TEMP");

        // Taken from: https://stackoverflow.com/questions/12105691/convert-timestamp-datatype-into-unix-timestamp-oracle
        dbHelper.executeSQLDirect("create or replace function date_to_unix_ts( PDate in date ) return number is\n"
                + "   l_unix_ts number;\n"
                + "begin\n"
                + "   l_unix_ts := round(( PDate - date '1970-01-01' ) * 60 * 60 * 24);\n"
                + "   return l_unix_ts;\n"
                + "end;");

        String createView = "CREATE VIEW OBSERVATION_FACT_TEMP AS SELECT\n"
                + "ENCOUNTER_NUM,\n"
                + "PATIENT_NUM,\n"
                + "CONCEPT_CD,\n"
                + "PROVIDER_ID,\n"
                + "START_DATE,\n"
                + "MODIFIER_CD,\n"
                + "INSTANCE_NUM,\n"
                + "VALTYPE_CD,\n"
                + "TVAL_CHAR,\n"
                + "NVAL_NUM,\n"
                + "VALUEFLAG_CD,\n"
                + "QUANTITY_NUM,\n"
                + "UNITS_CD,\n"
                + "END_DATE,\n"
                + "LOCATION_CD,\n"
                + "OBSERVATION_BLOB,\n"
                + "CONFIDENCE_NUM,\n"
                + "UPDATE_DATE,\n"
                + "DOWNLOAD_DATE,\n"
                + "IMPORT_DATE,\n"
                + "SOURCESYSTEM_CD,\n"
                + "UPLOAD_ID,\n"
                + "date_to_unix_ts(START_DATE) START_DATE_UNIX,\n"
                + "date_to_unix_ts(END_DATE) END_DATE_UNIX\n"
                + "FROM (";

        for (int a = 0; a < i2b2Vars.size(); a++) {
            createView += sqlStatements.get(a);
            if (a < i2b2Vars.size() - 1) {
                createView += "\n UNION ALL ";
            }
        }
        createView += ")";
        dbHelper.executeSQLDirect(createView);

        dbHelper.executeSQLDirect("ALTER VIEW OBSERVATION_FACT_TEMP ADD CONSTRAINT OBSERVATION_FACT_TEMP_PK PRIMARY KEY (\"PATIENT_NUM\", \"CONCEPT_CD\", \"MODIFIER_CD\", \"START_DATE\", \"ENCOUNTER_NUM\", \"INSTANCE_NUM\", \"PROVIDER_ID\") DISABLE NOVALIDATE");

        dbHelper.executeSQLDirect("DROP VIEW OBSERVATION_FACT_TEMP2");

        createView = "CREATE VIEW OBSERVATION_FACT_TEMP2 AS SELECT \n"
                + "CONCEPT_DIMENSION.NAME_CHAR CONCEPT,\n"
                + "OBSERVATION_FACT_TEMP.ENCOUNTER_NUM,\n"
                + "OBSERVATION_FACT_TEMP.PATIENT_NUM,\n"
                + "OBSERVATION_FACT_TEMP.CONCEPT_CD,\n"
                + "OBSERVATION_FACT_TEMP.PROVIDER_ID,\n"
                + "OBSERVATION_FACT_TEMP.START_DATE,\n"
                + "OBSERVATION_FACT_TEMP.MODIFIER_CD,\n"
                + "OBSERVATION_FACT_TEMP.INSTANCE_NUM,\n"
                + "OBSERVATION_FACT_TEMP.VALTYPE_CD,\n"
                + "OBSERVATION_FACT_TEMP.TVAL_CHAR,\n"
                + "OBSERVATION_FACT_TEMP.NVAL_NUM,\n"
                + "OBSERVATION_FACT_TEMP.VALUEFLAG_CD,\n"
                + "OBSERVATION_FACT_TEMP.QUANTITY_NUM,\n"
                + "OBSERVATION_FACT_TEMP.UNITS_CD,\n"
                + "OBSERVATION_FACT_TEMP.END_DATE,\n"
                + "OBSERVATION_FACT_TEMP.LOCATION_CD,\n"
                + "OBSERVATION_FACT_TEMP.OBSERVATION_BLOB,\n"
                + "OBSERVATION_FACT_TEMP.CONFIDENCE_NUM,\n"
                + "OBSERVATION_FACT_TEMP.UPDATE_DATE,\n"
                + "OBSERVATION_FACT_TEMP.DOWNLOAD_DATE,\n"
                + "OBSERVATION_FACT_TEMP.IMPORT_DATE,\n"
                + "OBSERVATION_FACT_TEMP.SOURCESYSTEM_CD,\n"
                + "OBSERVATION_FACT_TEMP.UPLOAD_ID,\n"
                + "OBSERVATION_FACT_TEMP.START_DATE_UNIX,\n"
                + "OBSERVATION_FACT_TEMP.END_DATE_UNIX\n"
                + "FROM OBSERVATION_FACT_TEMP, CONCEPT_DIMENSION\n"
                + "WHERE OBSERVATION_FACT_TEMP.CONCEPT_CD = CONCEPT_DIMENSION.CONCEPT_CD";

        dbHelper.executeSQLDirect(createView);
        dbHelper.closeConnection();

        log("Created views OBSERVATION_FACT_TEMP and OBSERVATION_FACT_TEMP2 for this i2b2 query.");

    }

    private void runUploadData() {

        log("Uploading data to RDF triple store ... ");

        //uploadRDF("C:\\Users\\matesn\\ownCloud\\Development\\d2rq-0.8.1\\i2b2.rdf", "http://localhost:3030/i2b2");
        String selectAll = "CONSTRUCT {\n"
                + "?s ?p ?o\n"
                + "}WHERE {\n"
                + "?s ?p ?o\n"
                + "}";

        execConstructAndPut("http://localhost:2020/sparql", "http://localhost:3030/i2b2", selectAll);
        updateTripleCount();

    }

    private void runLinkIntervals() {
        if (simpleAgg.isSelected()) {
            String linkNearIntervals = readFile("SPARQL/LinkNearIntervalsAndMarkAggregated.rq").replaceAll("#LINKTHRES#", linkThreshold.getText());
            log("Linking near intervals ...");
            System.out.println(linkNearIntervals);
            execConstructAndAdd("http://localhost:3030/i2b2", "http://localhost:3030/i2b2", linkNearIntervals);
            updateTripleCount();
        } else {

            DefaultTableModel model = (DefaultTableModel) configTable.getModel();
            for (int a = 0; a < model.getRowCount(); a++) {

                String conceptIn = model.getValueAt(a, 0).toString();
                String conceptOut = model.getValueAt(a, 1).toString();
                String interval = model.getValueAt(a, 2).toString();

                String linkNearIntervals = readFile("SPARQL/LinkNearIntervalsAndMarkAggregated.rq").replaceAll("#LINKTHRES#", interval);

                linkNearIntervals = linkNearIntervals.replaceAll("\\?interval1 i:hasConcept \\?sameConcept", "\\?interval1 i:hasConcept \"" + conceptOut + "\"");
                linkNearIntervals = linkNearIntervals.replaceAll("\\?interval2 i:hasConcept \\?sameConcept", "\\?interval2 i:hasConcept \"" + conceptOut + "\"");

                System.out.println(linkNearIntervals);
                //JOptionPane.showMessageDialog(null, "Concept: " + concept + " Time: " + interval, "Info", JOptionPane.INFORMATION_MESSAGE);

                log("Concept \"" + conceptOut + "\": Linking near intervals ... ");
                execConstructAndAdd("http://localhost:3030/i2b2", "http://localhost:3030/i2b2", linkNearIntervals);
                updateTripleCount();
            }
        }
    }

    private void runCreateSuperIntervals() {

        if (simpleAgg.isSelected()) {

            String createSuperIntervals = readFile("SPARQL/CreateSuperintervals.rq");
            log("Aggregating intervals into episodes ... ");
            System.out.println(createSuperIntervals);
            execConstructAndAdd("http://localhost:3030/i2b2", "http://localhost:3030/i2b2", createSuperIntervals);
            updateTripleCount();

        } else {

            DefaultTableModel model = (DefaultTableModel) configTable.getModel();
            for (int a = 0; a < model.getRowCount(); a++) {
                String conceptIn = model.getValueAt(a, 0).toString();
                String conceptOut = model.getValueAt(a, 1).toString();

                String createSuperIntervals = readFile("SPARQL/CreateSuperintervals.rq");

                createSuperIntervals = createSuperIntervals.replaceAll("\\?newURI i:hasConcept \\?sameConcept", "\\?newURI i:hasConcept \"" + conceptOut + "\"");
                createSuperIntervals = createSuperIntervals.replaceAll("\\?interval1 i:hasConcept \\?sameConcept", "\\?interval1 i:hasConcept \"" + conceptOut + "\"");
                createSuperIntervals = createSuperIntervals.replaceAll("\\?interval2 i:hasConcept \\?sameConcept", "\\?interval2 i:hasConcept \"" + conceptOut + "\"");

                System.out.println(createSuperIntervals);
                //JOptionPane.showMessageDialog(null, "Concept: " + concept + " Time: " + interval, "Info", JOptionPane.INFORMATION_MESSAGE);

                log("Concept \"" + conceptOut + "\": Aggregating intervals into episodes ... ");
                execConstructAndAdd("http://localhost:3030/i2b2", "http://localhost:3030/i2b2", createSuperIntervals);
                updateTripleCount();
            }
        }
    }

    private void runInferAllen() {

        log("\nAugmenting RDF data with Allen's relations:\n");
        Iterator it = FileUtils.iterateFiles(new File("SPARQL/Allen"), null, false);
        while (it.hasNext()) {

            String file = ((File) it.next()).getAbsolutePath();

            String allen = readFile(file).replaceAll("#UNCERTAINTY#", uncertaintyThreshold.getText());
            allen = allen.replaceAll("\\?conceptDimension", "#\\?conceptDimension");
            log("Executing " + file + " ... ");
            execConstructAndAdd("http://localhost:3030/i2b2", "http://localhost:3030/i2b2", allen);
            updateTripleCount();

            //if (doAllenSubintervals.isSelected()) {
            //    allen = allen.replaceAll("FILTER NOT EXISTS", "#FILTER NOT EXISTS");
            //    allen = allen.replaceAll("FILTER NOT EXISTS", "#FILTER NOT EXISTS");
            //}
        }
    }

    static void uploadRDF(String rdf, String serviceURI) {

        if (NoSPARQL) {
            return;
        }
        // parse the file
        Model m = ModelFactory.createDefaultModel();
        try (FileInputStream in = new FileInputStream(rdf)) {
            m.read(in, null, "RDF/XML");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // upload the resulting model
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI);
        accessor.putModel(m);
    }

    static String readFile(String filename) {
        File f = new File(filename);
        try {
            byte[] bytes = Files.readAllBytes(f.toPath());
            return new String(bytes, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    String execSelectAndProcess(String serviceURI, String query) {
        if (NoSPARQL) {
            return "";
        }
        String s = "";
        try {
            QueryExecution q = QueryExecutionFactory.sparqlService(serviceURI, query);
            ResultSet results = q.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                RDFNode result = soln.get("result");
                s += result.toString() + "\n";
            }
            q.close();
        } catch (Exception e) {
            e.printStackTrace();
            handleSparqlNotReady();
        }
        return s;
    }

    String explainPlan(String query) {
        String s = query;

        // Parse
        Query query2 = QueryFactory.create(s);
        System.out.println(query2);

        // Generate algebra
        Op op = Algebra.compile(query2);
        //JOptionPane.showMessageDialog(null, op.toString(), "Unoptimized", JOptionPane.INFORMATION_MESSAGE);
        op = Algebra.optimize(op);
        System.out.println(op);
        JOptionPane.showMessageDialog(null, op.toString(), "Optimized", JOptionPane.INFORMATION_MESSAGE);

        Query q = OpAsQuery.asQuery(op);
        String r = q.serialize();

        //JOptionPane.showMessageDialog(null, r, "Optimized SPARQL", JOptionPane.INFORMATION_MESSAGE);
        return r;
    }

    void execConstructAndAdd(String serviceURI1, String serviceURI2, String query) {
        if (NoSPARQL) {
            return;
        }
        try {
            Model results = QueryExecutionFactory.sparqlService(serviceURI1, query).execConstruct();
            DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI2);
            accessor.add(results);
            results.close();

        } catch (Exception e) {
            e.printStackTrace();
            handleSparqlNotReady();
        }
    }

    void execConstructAndPut(String serviceURI1, String serviceURI2, String query) {
        if (NoSPARQL) {
            return;
        }
        try {
            Model results = QueryExecutionFactory.sparqlService(serviceURI1, query).execConstruct();
            DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(serviceURI2);
            accessor.putModel(results);
            results.close();
        } catch (Exception e) {
            e.printStackTrace();
            handleSparqlNotReady();
        }
    }

    private static String simplifyNumber(String s) {
        // TODO Auto-generated method stub
        return s.replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#integer", "").replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#decimal", "").replaceAll("\n", "").trim();
    }

    private static String simplifyNumbers(String s) {
        // TODO Auto-generated method stub
        return s.replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#integer", "").replaceAll("\\^\\^http://www.w3.org/2001/XMLSchema#decimal", "").trim();
    }

    private void updateTripleCount() {
        if (NoSPARQL) {
            return;
        }
        String countTriples = readFile("SPARQL/Count.rq");
        String triples = execSelectAndProcess("http://localhost:3030/i2b2", countTriples);

        String str = logWindow.getText();
        if (str.length() > 0) {
            str = str.substring(0, str.length() - 1);
            logWindow.setText(str);
        }

        log(simplifyNumber(triples) + " triples");

        tripleCount.setText("Triples: " + simplifyNumber(triples));
        if (simplifyNumber(triples).equals("")) {
            tripleCount.setText("Triples: unknown");
        }
    }

    private void runLinkSubintervals() {
        String linkSubintervals = readFile("SPARQL/LinkSubintervals.rq");
        execConstructAndAdd("http://localhost:3030/i2b2", "http://localhost:3030/i2b2", linkSubintervals);
        linkSubintervals = readFile("SPARQL/aggSubintervalValues.rq");
        execConstructAndAdd("http://localhost:3030/i2b2", "http://localhost:3030/i2b2", linkSubintervals);
        updateTripleCount();
    }

    private void refreshConfigTable() {
        if (NoSQL == true) {
            return;
        }
        dbHelper dbHelper = new dbHelper();
        dbHelper.loadConfigFile("DBConnection.properties");
        dbHelper.initConnention();
        dbHelper.executeSQL("SELECT NAME, REQUEST_XML, GENERATED_SQL FROM QT_QUERY_MASTER WHERE NAME = '" + previousQueries.getSelectedItem() + "'");
        dbHelper.nextEntry();
        i2b2Helper helper2 = new i2b2Helper();
        helper2.processI2b2Query(dbHelper.getColumn("REQUEST_XML"));
        dbHelper.closeConnection();

        List<String> i2b2Vars = helper2.getI2b2Variables();
        DefaultTableModel model = (DefaultTableModel) configTable.getModel();
        deleteAllRows(model);
        for (int a = 0; a < i2b2Vars.size(); a++) {
            String data1 = i2b2Vars.get(a);
            String data2 = i2b2Vars.get(a) + " Agg";
            String data3 = "60*60*24";
            Object[] row = {data1, data2, data3};
            model.addRow(row);
        }
    }

    private void testConfigTable() {
        DefaultTableModel model = (DefaultTableModel) configTable.getModel();
        duplicateIntervals.setSelected(false);
        for (int a = 0; a < model.getRowCount(); a++) {
            String conceptIn = model.getValueAt(a, 0).toString();
            String conceptOut = model.getValueAt(a, 1).toString();
            if (!conceptIn.equals(conceptOut)) {
                duplicateIntervals.setSelected(true);
            }
        }
    }

    private void runDuplicateIntervals() {
        DefaultTableModel model = (DefaultTableModel) configTable.getModel();
        for (int a = 0; a < model.getRowCount(); a++) {
            String conceptIn = model.getValueAt(a, 0).toString();
            String conceptOut = model.getValueAt(a, 1).toString();
            if (!conceptIn.equals(conceptOut) && !conceptIn.trim().equals("")) {

                String duplicateIntervals = readFile("SPARQL/DuplicateIntervals.rq");
                duplicateIntervals = duplicateIntervals.replaceAll("#IN#", conceptIn);
                duplicateIntervals = duplicateIntervals.replaceAll("#OUT#", conceptOut);
                System.out.println(duplicateIntervals);
                //JOptionPane.showMessageDialog(null, "Concept: " + concept + " Time: " + interval, "Info", JOptionPane.INFORMATION_MESSAGE);

                log("Concept \"" + conceptIn + "\": Duplicating renamed intervals ...");
                execConstructAndAdd("http://localhost:3030/i2b2", "http://localhost:3030/i2b2", duplicateIntervals);
                updateTripleCount();
            }
        }
    }

    private void runCreateSimplified() {
        String sparql = readFile("SPARQL/SimplifyDur.rq");
        log("Creating simplified 'dur' and 'con' relations ... ");
        System.out.println(sparql);
        execConstructAndAdd("http://localhost:3030/i2b2", "http://localhost:3030/i2b2", sparql);
        updateTripleCount();
        sparql = readFile("SPARQL/SimplifyAft.rq");
        log("Creating simplified 'aft' and 'bef' relations ... ");
        System.out.println(sparql);
        execConstructAndAdd("http://localhost:3030/i2b2", "http://localhost:3030/i2b2", sparql);
        updateTripleCount();
    }

    private void autoGenSPARQL() {

        TranslatorStatus.setText("WORKING");
        TranslatorStatus.setBackground(Color.MAGENTA);

        runSPARQL.setEnabled(false);

        String Allen = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader("TemporalLogic.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                Allen += line + ";";
            }
            br.close();
        } catch (Exception ex) {
            Logger.getLogger(MainForm.class.getName()).log(Level.SEVERE, null, ex);
        }

        log("Processing new query: \n\n" + Allen.replaceAll(";", "\n") + "");

        String relativeTemporalCriteria = "";

        // Derive relative constraints:
        if (!Allen.trim().replaceAll(";", "").equals("") && !Allen.contains("Temporal Error")) {
            log("Preparing to compute relative temporal constraints via SageMathCell ...");
            RelativeConstraintsProcessor rcp = new RelativeConstraintsProcessor(Allen);
            String result = rcp.process();

            if (!result.equals("")) {
                log("SageMath expression to evaluate is:\n");
                log(result + "\n");
                log("Calling SageMathCell ...");
                relativeTemporalCriteria = rcp.callSageMath(result);
                System.out.println("Relative temporal criteria: " + relativeTemporalCriteria);
                relativeTemporalCriteria = rcp.makeSparqlFilter(relativeTemporalCriteria);
                log("... Done!");

                if (relativeTemporalCriteria.equals("")) {
                    log("SageMathCell did not return a result when computing the \"constraints on relative intervals\" SPARQL section. Please check for invalid temporal patterns involving duration intervals (e.g. \"3 Months\" during \"2 Months\")!");
                    JOptionPane.showMessageDialog(null, "SageMathCell did not return a result when computing the \"constraints on relative intervals\" SPARQL section.\nPlease check for invalid temporal patterns involving duration intervals (e.g. \"3 Months\" during \"2 Months\")!", "Temporal Error", JOptionPane.ERROR_MESSAGE);
                    relativeTemporalCriteria = "  # ERROR: SageMathCell did not return a result when computing the \"constraints on relative\n"
                            + "  #        intervals\" SPARQL section. Please check for invalid temporal patterns involving\n"
                            + "  #        duration intervals (e.g. \"3 Months\" during \"2 Months\")!";
                }

            } else {
                log("There are no relative temporal constraints in this query.\n");
            }

        }

        // Generate and execute SPARQL:
        patientCount.setText("Patients: unknown");
        log("Translating query pattern into a SPARQL query ...");
        String SPARQLQuery = "";
        try {
            if (!Allen.trim().replaceAll(";", "").equals("") && !Allen.contains("Temporal Error")) {

                System.out.println("Allen: " + Allen);
                SPARQLTranslator translator = new SPARQLTranslator(Allen);
                System.out.println();
                translator.setRelativeConstraints(relativeTemporalCriteria);
                SPARQLQuery = translator.translate();
                log("... Done!");
                
                if (!SPARQLQuery.trim().equals("") && !SPARQLQuery.equals(";")) {
                    SPARQLcode.setText(SPARQLQuery.trim());

                    if (autoRunSPARQL.isSelected()) {
                        runTheSPARQLQuery();
                    }
                } else {
                    log("  *** CANNOT PROCESS THIS QUERY! ***\n");
                    patientCount.setText("Patients: 0");
                }

            } else {
                log("  *** CANNOT PROCESS THIS QUERY! ***\n");
                patientCount.setText("Patients: 0");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (SPARQLQuery.contains("WARNING: ")) {
            TranslatorStatus.setText("SPARQL WARNING");
            TranslatorStatus.setBackground(Color.YELLOW);
        }
        if (SPARQLQuery.contains("ERROR: ")) {
            TranslatorStatus.setText("SPARQL ERROR");
            TranslatorStatus.setBackground(Color.RED);
        }
        if (TranslatorStatus.getText().equals("WORKING")) {
            TranslatorStatus.setText("READY");
            TranslatorStatus.setBackground(Color.GREEN);
        }
        DefaultCaret caret = (DefaultCaret) SPARQLcode.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        runSPARQL.setEnabled(true);

    }

    private void runTheSPARQLQuery() {

        runSPARQL.setEnabled(false);

        TranslatorStatus.setText("WORKING");
        TranslatorStatus.setBackground(Color.MAGENTA);
        patientCount.setText("Patients: (working)");

        String SPARQLQuery = SPARQLcode.getText();

        System.out.println("Executing SPARQL query:\n\n" + SPARQLQuery);

        log("Executing SPARQL query.\n");
        long createdMillis = System.currentTimeMillis();
        String patientList = simplifyNumbers(execSelectAndProcess("http://localhost:3030/i2b2", SPARQLQuery));
        long nowMillis = System.currentTimeMillis();

        if (!patientList.equals("")) {

            log("Patient IDs: " + patientList.replaceAll("\n", ", ") + "\n");

            int count = patientList.length() - patientList.replace("\n", "").length() + 1;
            log("Patients: " + count);
            patientCount.setText("Patients: " + count);

        } else {
            log("  *** NO RESULT ***\n");
            patientCount.setText("Patients: 0");
        }

        log("Execution time: " + (((double) nowMillis - (double) createdMillis) / 1000.0) + " seconds.\n");

        if (TranslatorStatus.getText().equals("WORKING")) {
            TranslatorStatus.setText("DONE");
            TranslatorStatus.setBackground(Color.GREEN);
        }

        runSPARQL.setEnabled(true);
    }

}
