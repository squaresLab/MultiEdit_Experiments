import org.junit.*;
public class Testa{
    
    @Test
    public void Test1(){
        Assert.assertTrue(true);
        Assert.assertFalse(false);
    }
    
    @Test
    public void Test2(){
        Assert.assertTrue(false);
        Assert.assertFalse(true);
    }
    
    @Test
    public void Test3(){
        Assert.assertEquals(1,2);
        Assert.assertEquals(1.1,1.2,0.01);
    }
    
    @Test
    public void Test4(){
        Assert.assertEquals("asb","bsb");
    }
    
    @Test
    public void Test5(){
        Assert.assertEquals("poi","bsb");
    }
}
