import java.awt.*;

public class Beeper{
  public static void main(String[] args){
    try{Toolkit.getDefaultToolkit().beep();}catch(Throwable e){}
  }
}
