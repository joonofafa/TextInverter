import javax.swing.*;

public class MainFrame extends JFrame
{
    private JTextArea textMain;
    private JPanel pnMain;

    public void setDefault()
    {
        this.setContentPane(pnMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void setPopupMenu(JPopupMenu jpMenu)
    {
        textMain.setComponentPopupMenu(jpMenu);
    }

    public String getText()
    {
        if (textMain.getText() != null)
        {
            return textMain.getText();
        }

        return "";
    }

    public void setText(String strText)
    {
        textMain.setText(strText);
    }
}
