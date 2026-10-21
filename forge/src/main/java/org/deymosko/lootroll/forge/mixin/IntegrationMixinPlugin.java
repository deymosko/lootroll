package org.deymosko.lootroll.forge.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies integration mixins only when the mod they target is installed,
 * so missing optional mods don't produce mixin errors.
 */
public class IntegrationMixinPlugin implements IMixinConfigPlugin {

    private static final String PACKAGE = "org.deymosko.lootroll.forge.mixin.";

    // sub-package -> required mod id
    private static final Map<String, String> REQUIRED_MODS = Map.of(
            "alexsCaves.", "alexscaves",
            "dungeonNowLoading.", "dungeonnowloading",
            "iceAndFire.", "iceandfire"
    );

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String relative = mixinClassName.startsWith(PACKAGE) ? mixinClassName.substring(PACKAGE.length()) : mixinClassName;
        for (Map.Entry<String, String> entry : REQUIRED_MODS.entrySet()) {
            if (relative.startsWith(entry.getKey())) {
                return LoadingModList.get().getModFileById(entry.getValue()) != null;
            }
        }
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
