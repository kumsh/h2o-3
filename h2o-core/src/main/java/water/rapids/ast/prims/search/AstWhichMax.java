package water.rapids.ast.prims.search;

import water.Futures;
import water.MRTask;
import water.fvec.*;
import water.rapids.Env;
import water.rapids.vals.ValFrame;
import water.rapids.ast.AstPrimitive;
import water.rapids.ast.AstRoot;

/**
 * Index of which entry contains the max value
 */
public class AstWhichMax extends AstPrimitive {
    @Override
    public String[] args() {
        return new String[]{"ary"};
    }

    @Override
    public int nargs() {
        return 1 + 1;
    } // (which.max col)

    @Override
    public String str() {
        return "which.max";
    }

    @Override
    public ValFrame apply(Env env, Env.StackHelp stk, AstRoot asts[]) {
        Frame f = stk.track(asts[1].exec(env)).getFrame();

        // Get max index for 1 row
        if (f.numRows() == 1 && f.numCols() > 1) {
            AppendableVec v = new AppendableVec(Vec.VectorGroup.VG_LEN1.addVec(), Vec.T_NUM);
            NewChunk chunk = new NewChunk(v, 0);
            double max = Double.NEGATIVE_INFINITY;;
            int maxIndex = 0;
            for (int i = 0; i < f.numCols(); i++) {
                if (f.vecs()[i].at8(0) > max) {
                    max = f.vecs()[i].at8(0);
                    maxIndex = i;
                }
            }
            chunk.addNum(maxIndex);
            Futures fs = chunk.close(0, new Futures());
            Vec vec = v.layout_and_close(fs);
            fs.blockForPending();
            return new ValFrame(new Frame(vec));
        }

        // Get max index for 1 column
        Vec vec = f.anyVec();
        final double max = vec.max();
        if (f.numCols() > 1 || !vec.isNumeric())
            throw new IllegalArgumentException("which.max requires a single integer column");
        Frame f2 = new MRTask() {
            @Override
            public void map(Chunk c, NewChunk nc) {
                long start = c.start();
                for (int i = 0; i < c._len; ++i)
                    if (c.at8(i) == max) nc.addNum(start + i);
            }
        }.doAll(new byte[]{Vec.T_NUM}, vec).outputFrame();
        return new ValFrame(f2);
    }
}
