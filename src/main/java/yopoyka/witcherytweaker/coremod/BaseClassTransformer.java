package yopoyka.witcherytweaker.coremod;

import net.minecraft.launchwrapper.IClassTransformer;

import java.util.HashMap;
import java.util.Map;

public class BaseClassTransformer implements IClassTransformer {
    protected Map<String, IClassTransformer> transformers = new HashMap<>();

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformers.isEmpty()) return basicClass;

        final IClassTransformer transformer = transformers.remove(transformedName);
        if (transformer != null)
            return transformer.transform(name, transformedName, basicClass);

        return basicClass;
    }
}
