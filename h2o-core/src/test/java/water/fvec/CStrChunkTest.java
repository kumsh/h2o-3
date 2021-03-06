package water.fvec;

import org.junit.*;

import water.IcedUtils;
import water.TestUtil;
import water.parser.BufferedString;

import java.util.Arrays;

public class CStrChunkTest extends TestUtil {
  @BeforeClass() public static void setup() { stall_till_cloudsize(1); }
  @Test
  public void test_inflate_impl() {
    for (int l=0; l<2; ++l) {
      NewChunk nc = new NewChunk(null, 0);

      BufferedString[] vals = new BufferedString[1000001];
      for (int i = 0; i < vals.length; i++) {
        vals[i] = new BufferedString("Foo"+i);
      }
      if (l==1) nc.addNA();
      for (BufferedString v : vals) nc.addStr(v);
      nc.addNA();
      int len = nc.len();
      Chunk cc = nc.compress();
      Assert.assertEquals(vals.length + 1 + l, cc._len);
      Assert.assertTrue(cc instanceof CStrChunk);
      if (l==1) Assert.assertTrue(cc.isNA(0));
      if (l==1) Assert.assertTrue(cc.isNA_abs(0));
      BufferedString tmpStr = new BufferedString();
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc.atStr(tmpStr, l + i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc.atStr_abs(tmpStr, l + i));
      Assert.assertTrue(cc.isNA(vals.length + l));
      Assert.assertTrue(cc.isNA_abs(vals.length + l));

      Chunk cc2 = IcedUtils.deepCopy(cc);
      Assert.assertEquals(vals.length + 1 + l, cc2._len);
      Assert.assertTrue(cc2 instanceof CStrChunk);
      if (l==1) Assert.assertTrue(cc2.isNA(0));
      if (l==1) Assert.assertTrue(cc2.isNA_abs(0));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc2.atStr(tmpStr, l + i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc2.atStr_abs(tmpStr, l + i));
      Assert.assertTrue(cc2.isNA(vals.length + l));
      Assert.assertTrue(cc2.isNA_abs(vals.length + l));

      nc = cc.extractRows(new NewChunk(null, 0),0,len);
      Assert.assertEquals(vals.length + 1 + l, nc._len);

      if (l==1) Assert.assertTrue(nc.isNA(0));
      if (l==1) Assert.assertTrue(nc.isNA_abs(0));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], nc.atStr(tmpStr, l + i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], nc.atStr_abs(tmpStr, l + i));
      Assert.assertTrue(nc.isNA(vals.length + l));
      Assert.assertTrue(nc.isNA_abs(vals.length + l));

      cc2 = nc.compress();
      Assert.assertEquals(vals.length + 1 + l, cc._len);
      Assert.assertTrue(cc2 instanceof CStrChunk);
      if (l==1) Assert.assertTrue(cc2.isNA(0));
      if (l==1) Assert.assertTrue(cc2.isNA_abs(0));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc2.atStr(tmpStr, l + i));
      for (int i = 0; i < vals.length; ++i) Assert.assertEquals(vals[i], cc2.atStr_abs(tmpStr, l + i));
      Assert.assertTrue(cc2.isNA(vals.length + l));
      Assert.assertTrue(cc2.isNA_abs(vals.length + l));

      Assert.assertTrue(Arrays.equals(cc._mem, cc2._mem));
    }
  }

  @Test
  public void test_writer(){
    Frame frame = null;
    try {
      frame = parse_test_file("smalldata/junit/iris.csv");

      //Create a label vector
      byte[] typeArr = {Vec.T_STR};
      Vec labels = frame.lastVec().makeCons(1, 0, null, typeArr)[0];
      Vec.Writer writer = labels.open();
      int rowCnt = (int)frame.lastVec().length();
      for (int r = 0; r < rowCnt; r++) // adding labels in reverse order
        writer.set(rowCnt-r-1, "Foo"+(r+1));
      writer.close();

      //Append label vector and spot check
      frame.add("Labels", labels);
      Assert.assertTrue("Failed to create a new String based label column", frame.lastVec().atStr(new BufferedString(), 42).compareTo(new BufferedString("Foo108"))==0);
    } finally {
      if (frame != null) frame.delete();
    }
  }

  @Test
  public void test_sparse() {
    NewChunk nc = new NewChunk(null, 0);
    for( int i=0; i<100; i++ )
      nc.addNA();
    nc.addStr(new BufferedString("foo"));
    nc.addNA();
    nc.addStr(new BufferedString("bar"));
    Chunk c = nc.compress();
    Assert.assertTrue("first 100 entries are NA",c.isNA(0) && c.isNA(99));
    Assert.assertTrue("Sparse string has values",c.atStr(new BufferedString(),100).sameString("foo"));
    Assert.assertTrue("NA",c.isNA(101));
    final BufferedString bufferedString = c.atStr(new BufferedString(), 102);
    Assert.assertTrue("Sparse string has values: expected `bar`, got " + bufferedString, bufferedString.sameString("bar"));
  }
}


