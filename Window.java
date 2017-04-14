import javax.swing.*;
import javax.swing.colorchooser.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.lang.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import java.util.ArrayList;

enum Mode{
    RECTANGLE, CIRCLE, POLYGON, EDIT;
}

class Window extends JFrame implements ActionListener{
    public static Mode mode;
    public static Color rgb=Color.BLACK;
    public static JPanel canvas;

    public Color c;
    private JFrame infoWindow;
    private JPanel toolbox;
    private JButton info,rectangle,circle,color,polygon,edit;

    public Window(){
        super("Paint");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700,400);
        setLayout(new BorderLayout());

        createToolbox();
        createCanvas();
        createInfoWindow();

        setVisible(true);
    }

    private void createToolbox(){
        JPanel left,right;
        toolbox = new JPanel(new GridLayout(1,2));
        add(toolbox,BorderLayout.NORTH);
        left = new JPanel(new FlowLayout(FlowLayout.LEADING));
        right = new JPanel(new FlowLayout(FlowLayout.TRAILING));

        toolbox.add(left);
        toolbox.add(right);

        edit = new JButton("E");
        edit.addActionListener(this);
        left.add(edit);

        rectangle = new JButton("R");
        rectangle.addActionListener(this);
        left.add(rectangle);

        circle = new JButton("O");
        circle.addActionListener(this);
        left.add(circle);

        polygon = new JButton("P");
        polygon.addActionListener(this);
        left.add(polygon);

        color = new JButton();
        color.addActionListener(this);
        color.setBackground(rgb);
        color.setPreferredSize(new Dimension(20, 20));
        color.setBorder(BorderFactory.createEmptyBorder());
        color.setOpaque(true);
        left.add(color);

        info = new JButton("Informacje");
        info.addActionListener(this);
        right.add(info);
    }

    private void createCanvas(){
        canvas = new CanvasPanel();
        add(canvas,BorderLayout.CENTER);
    }

    private void createInfoWindow(){
        infoWindow = new JFrame("Informacje");
        infoWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        infoWindow.setSize(300,150);
        infoWindow.setResizable(false);
        infoWindow.setLayout(new FlowLayout());

        infoWindow.add(new JLabel("<html><center style='margin-top: 30px;'>Prosty program do edycji grafiki.<br/>&copy; Maciej Stosio</center></html>"));
    }

    private void createColorPicker(){
        JColorChooser chooser = new JColorChooser();
        AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
        ActionListener setColor = new ActionListener() {
          public void actionPerformed(ActionEvent actionEvent) {
            rgb = chooser.getColor();
            color.setBackground(rgb);
          }
        };

        for (AbstractColorChooserPanel accp : panels) {
            if(!accp.getDisplayName().equals("RGB")) {
                chooser.removeChooserPanel(accp);
            }
        }

        JColorChooser.createDialog(null, "Wybierz kolor", false, chooser, setColor , null).setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if(source != info && source != color){
            circle.setForeground(Color.BLACK);
            polygon.setForeground(Color.BLACK);
            rectangle.setForeground(Color.BLACK);
            edit.setForeground(Color.BLACK);
        }

        if(source==rectangle){
            if(Window.mode!=Mode.RECTANGLE){
                rectangle.setForeground(Color.BLUE);
                Window.mode = Mode.RECTANGLE;
            }else{
                rectangle.setForeground(Color.BLACK);
                Window.mode = null;
            }
        }else if(source==circle){
            if(Window.mode!=Mode.CIRCLE){
                circle.setForeground(Color.BLUE);
                Window.mode = Mode.CIRCLE;
            }else{
                circle.setForeground(Color.BLACK);
                Window.mode = null;
            }
        }else if(source==polygon){
            if(Window.mode!=Mode.POLYGON){
                polygon.setForeground(Color.BLUE);
                Window.mode = Mode.POLYGON;
            }else{
                polygon.setForeground(Color.BLACK);
                Window.mode = null;
            }
        }else if(source==edit){
            if(Window.mode!=Mode.EDIT){
                edit.setForeground(Color.BLUE);
                Window.mode = Mode.EDIT;
            }else{
                edit.setForeground(Color.BLACK);
                Window.mode = null;
            }
        }else if(source==info){
            infoWindow.setVisible(true);
        }else if(source==color){
            createColorPicker();
        }
    }
}

class CanvasPanel extends JPanel implements MouseListener, MouseMotionListener{
    private Graphics2D g2d;
    public static Shape selected;
    private int x, y;
    public static ArrayList<Shape> shapes = new ArrayList<Shape>();
    private ArrayList<Point> points= new ArrayList<Point>();

    CanvasPanel(){
        setBackground(Color.WHITE);
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyBindings();
    }

    private void addKeyBindings(){
        //BACKSPACE
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "delete");
        getActionMap().put("delete", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                for(int i=0;i<CanvasPanel.shapes.size();i++){
                    if(CanvasPanel.selected==CanvasPanel.shapes.get(i)){
                        CanvasPanel.selected=null;
                        CanvasPanel.shapes.remove(i);
                        Window.canvas.repaint();
                        System.out.println("DELETED");
                        break;
                    }
                }
            }
        });

        //SHIFT
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SHIFT"), "shift press");
        getActionMap().put("shift press", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("shift press");
            }
        });

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("released SHIFT"), "shift release");
        getActionMap().put("shift release", new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("shift release");
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Shape preview;
        super.paintComponent(g);
        g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, this.getWidth(), this.getHeight());

        for(Shape item: shapes) item.draw(g2d);

        if(Window.mode==Mode.RECTANGLE && points.size()>0){
            preview = new Rectangle(points);
            preview.preview(g2d,x,y);
        }else if(Window.mode==Mode.CIRCLE && points.size()>0){
            preview = new Circle(points);
            preview.preview(g2d,x,y);
        }else if(Window.mode==Mode.POLYGON){
            preview = new Polygon(points);
            preview.preview(g2d,x,y);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if(Window.mode==Mode.POLYGON){
            x= (int)e.getX();
            y= (int)e.getY();
        }
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if(Window.mode==Mode.POLYGON){
            Shape newShape;
            points.add(new Point((int)e.getX(), (int)e.getY()));
            if(e.getClickCount() == 2 && !e.isConsumed()) {
                e.consume();
                newShape = new Polygon(points);
                newShape.setColor(Window.rgb);
                if(newShape.valid()){
                    shapes.add(newShape);
                }else{
                    System.out.println("UNVALID");
                }
                points.clear();
            }
            x= (int)e.getX();
            y= (int)e.getY();
        }else if(Window.mode==Mode.EDIT){
            if (SwingUtilities.isRightMouseButton(e) || e.isControlDown()){
                if(selected!=null){
                    JColorChooser chooser = new JColorChooser();
                    AbstractColorChooserPanel[] panels = chooser.getChooserPanels();
                    ActionListener setColor = new ActionListener() {
                      public void actionPerformed(ActionEvent actionEvent) {
                        selected.setColor(chooser.getColor());
                        Window.canvas.repaint();
                      }
                    };

                    for (AbstractColorChooserPanel accp : panels) {
                        if(!accp.getDisplayName().equals("RGB")) {
                            chooser.removeChooserPanel(accp);
                        }
                    }

                    JColorChooser.createDialog(null, "Wybierz kolor", false, chooser, setColor , null).setVisible(true);

                }
            }else{
                for(int i = shapes.size()-1; i>=0; i--){
                    // System.out.println(i);
                    if(shapes.get(i).include((int)e.getX(),(int)e.getY())){
                        if(shapes.get(i).isSelected()){
                            System.out.println("UNSELECT");
                            shapes.get(i).unselect();
                            selected=null;
                        }else{
                            System.out.println("SELECT");
                            shapes.get(i).select();
                            selected = shapes.get(i);
                        }
                        for(int k = i-1; k>=0; k--){
                            shapes.get(k).unselect();
                        }
                        break;
                    }else{
                        shapes.get(i).unselect();
                    }
                }
            }
        }
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if(Window.mode==Mode.RECTANGLE || Window.mode==Mode.CIRCLE){
            points.add(new Point((int)e.getX(), (int)e.getY()));
            x= (int)e.getX();
            y= (int)e.getY();
        }else if(Window.mode==Mode.EDIT){
            x= (int)e.getX();
            y= (int)e.getY();
        }
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(Window.mode==Mode.RECTANGLE || Window.mode==Mode.CIRCLE){
            x= (int)e.getX();
            y= (int)e.getY();
        }else if(Window.mode==Mode.EDIT){
            if(selected!=null){
                selected.move(e.getX()-x,e.getY()-y);
                x=(int)e.getX();
                y=(int)e.getY();
            }
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Shape newShape;
        if(Window.mode==Mode.RECTANGLE){
            points.add(new Point((int)e.getX(), (int)e.getY()));
            newShape = new Rectangle(points);
            newShape.setColor(Window.rgb);
            if(newShape.valid()){
                shapes.add(newShape);
            }else{
                System.out.println("UNVALID");
            }
            points.clear();
        }else if(Window.mode==Mode.CIRCLE){
            points.add(new Point((int)e.getX(), (int)e.getY()));
            newShape = new Circle(points);
            newShape.setColor(Window.rgb);
            if(newShape.valid()){
                shapes.add(newShape);
            }else{
                System.out.println("UNVALID");
            }
            points.clear();
        }
        repaint();
    }
}
