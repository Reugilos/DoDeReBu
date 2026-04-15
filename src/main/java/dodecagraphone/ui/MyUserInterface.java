package dodecagraphone.ui;

import dodecagraphone.MyController;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

/**
 * The user interface manages the main window (JFrame, Jpanel) and the mouse,
 * forwarding actions to the controller.
 */
public class MyUserInterface extends JFrame {
    private final MyController controller;
    private MyNewPanel panel;
    private String version;
    
    /**
     * Set up
     *
     * @param titol
     */
    public MyUserInterface() {
        super("DoDeReBu_App_v4.0");
        this.version = this.getTitle();
        this.controller = new MyController(this);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        panel = new MyNewPanel(this.controller);
        panel.setDoubleBuffered(false);
        panel.setIgnoreRepaint(true);
        this.add(panel);
        this.pack();
        this.setVisible(true);

        // Executar onExitCheckNSave() quan es tanqui la finestra
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (controller.onExitCheckNSave()) {
                    e.getWindow().dispose(); // només tanquem si l’usuari no ha cancel·lat
                    System.exit(0);
                }
            }
        });
    }

    public String getVersion() {
        return version;
    }

    
    public MyController getController(){
        return this.controller;
    }

    public MyNewPanel getPanel() {
        return panel;
    }
    
    /**
     * Invoked inside the main loop.
     */
    public void update() {
//            this.controller.update();
//            this.panel.repinta(true);
//
        if (this.controller.update() && !this.controller.getCam().isPlaying()) {
            this.panel.repinta(true);
        }
//        if (this.controller.update()) {
////            MyController.setDoRedraw(true);
////            try {
////                SwingUtilities.invokeAndWait(() -> this.repaint());
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
//            //this.panel.paintImmediately(this.panel.getBounds());
//            //this.validate(); 
//            System.out.println("MyUserInterface::update: controller.update is true, repaint is now called");
//            this.controller.setNeedsDrawing(true);
//            this.repaint(); // activa panel.paintComponent() ergo Controller.redraw()
//            //this.controller.getMixer().repaintMixer();
//        }
    }
    
}

///**
// * Manages drawing (paintComponent()), mouse and buttons. Drawing is done on the
// * JPanel (drawing on an Image of the JFrame produces flickering).
// *
// * @author upcnet
// */
//public class MyNewPanel extends JPanel implements ActionListener, KeyListener {
//
//    private MyController controller;
//    private JButton b, b2;
////    private Image buffer; // Imatge en memòria
////    private Graphics bufferGraphics; // Objecte Graphics per a la imatge en memòria
//
//    public MyNewPanel(MyController contr) {
// //               setPreferredSize(new Dimension(500, 500));
////        Dimension d = getPreferredSize();
////        buffer = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
////        bufferGraphics = buffer.getGraphics();
//
//        this.controller = contr;
//        this.setDoubleBuffered(true);        // Dibuixa els gràfics a la imatge en memòria
//        this.setBorder(BorderFactory.createLineBorder(Color.black));
//        this.addMouseListener(new MyNewMouseAdapter(this, this.controller));
//        this.addMouseMotionListener(new MyNewMouseAdapter(this, this.controller));
//
//        // ImageIcon leftButtonIcon = createImageIcon("images/right.gif");
//        this.b = new JButton("Test Button");//,leftButtonIcon);        
////        b1.setVerticalTextPosition(AbstractButton.CENTER);
////        b1.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
////        b1.setMnemonic(KeyEvent.VK_D);
//        this.b.setActionCommand("button");
//
//        //Listen for actions on buttons 1 and 3.
//        b.addActionListener(this);
//        b.setToolTipText("Click this button to show demo message.");
//
//        //Add Components to this container, using the default FlowLayout.
////        this.add(b);
//        this.b2 = new JButton("Test Button");
//        this.b2.setActionCommand("button2");
//        b2.addActionListener(this);
//        b2.setToolTipText("Click this button to show demo 2 message.");
////        this.add(b2);   
//        this.addKeyListener(this);
//    }
//
//    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension((int)Settings.getScreenWidth(),(int)Settings.getScreenHeight());
//    }
//
//    /**
//     * The key listener requires the focus on the JPanel.
//     */
//    @Override
//    public void addNotify() {
//        super.addNotify();
//        requestFocus();
//    }
//
//    private static int count = 0;
//    /**
//     * Main paint method. It is automatically invoked at each window change and
//     * when repaint() is called (either on the JFrame or the JPanel). It should
//     * not be explicitly called.
//     *
//     * @param g
//     */
//    @Override
//    public void paintComponent(Graphics g) {
//        super.paintComponent(g);
////        draw(bufferGraphics);
////        if (count%2==1) {
//            count ++;
////        if (this.controller.isNeedsDrawing()) {
////            MyController.setDoRedraw(true);
////            try {
////                SwingUtilities.invokeAndWait(() -> this.repaint());
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
//            //this.panel.paintImmediately(this.panel.getBounds());
//            //this.validate(); 
////            System.out.println("MyUserInterface::paintComponent: redraw is now called");
//            this.controller.redraw((Graphics2D) g);
//            //this.controller.setNeedsDrawing(false);
////            this.repaint(); // activa panel.paintComponent() ergo Controller.redraw()
//            //this.controller.getMixer().repaintMixer();
////        }
////            count = 0;
////        }
////        System.out.println("MyUserInterface::paintComponent: count = "+ count);
//
////        // Copia la imatge en memòria a la pantalla
////        g.drawImage(buffer, 0, 0, null);
//    }
//
////    private void draw(Graphics g) {
////        // Aquí pots posar el teu codi per dibuixar els gràfics
////    }
////
//    /**
//     * Action performed, triggered by a button event.
//     *
//     * @param e
//     */
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        if ("button".equals(e.getActionCommand())) {
//            this.controller.onDemoButtonPressed();
//            //        b.setEnabled(false);
//        } else if ("button2".equals(e.getActionCommand())) {
//            this.controller.onDemoButton2Pressed();
//            //        b.setEnabled(false);
//        }
//    }
//
////    /** Returns an ImageIcon, or null if the path was invalid. */
////    protected static ImageIcon createImageIcon(String path) {
////        java.net.URL imgURL = ButtonDemo.class.getResource(path);
////        if (imgURL != null) {
////            return new ImageIcon(imgURL);
////        } else {
////            System.err.println("Couldn't find file: " + path);
////            return null;
////        }
////    }
//    @Override
//    public void keyTyped(KeyEvent e) {
////        System.err.println("Key typed, not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//    @Override
//    public void keyPressed(KeyEvent e) {
//        if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
//            if (e.isShiftDown()) {
//                controller.redo();
//            } else {
////                System.out.println("Undo");
//                controller.undo();
//            }
//            this.repaint();
//        }
//    }
//
//    @Override
//    public void keyReleased(KeyEvent e) {
//        // System.err.println("Key released."); //To change body of generated methods, choose Tools | Templates.
//    }
//
//}
//
///**
// * Overrides callback methods invoked by mouse events.
// *
// * @author upcnet
// */
//class MyNewMouseAdapter extends MouseAdapter {
//
//    MyController controller;
//    JPanel panel;
//
//    public MyNewMouseAdapter(JPanel panel, MyController contr) {
//        super();
//        this.panel = panel;
//        this.controller = contr;
//    }
//
//    @Override
//    public void mouseMoved(MouseEvent e){
//        double posX = e.getX();
//        double posY = e.getY();
//        controller.onMouseMoved(posX, posY);
//        // this.panel.repaint();
//    }
//    
//    @Override
//    public void mouseDragged(MouseEvent e) {
////        System.err.println("MyMouseListener.mouseDragged()");
//        double posX = e.getX();
//        double posY = e.getY();
//        controller.onMouseDragged(posX, posY);
//        this.panel.repaint();
//    }
//
//    @Override
//    public void mousePressed(MouseEvent e) {
////        System.err.println("MyMouseListener.mousePressed()");
//        int pointer = e.getButton();
//        if (pointer == 1) {
//            double posX = e.getX();
//            double posY = e.getY();
//            this.controller.onMousePressed(posX, posY);
//            this.panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
//            this.panel.repaint();
//        }
//    }
//
//    @Override
//    public void mouseReleased(MouseEvent e) {
//        int pointer = e.getButton();
//        if (pointer == 1) {
//            double posX = e.getX();
//            double posY = e.getY();
//            this.controller.onMouseReleased(posX, posY);
//            this.panel.repaint();
//        }
//    }
//}
