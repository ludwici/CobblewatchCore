package com.ludwici.cobblewatchcore.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class CoreMixinPlugin implements IMixinConfigPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("com.ludwici.cobblewatchcore.mixin.weather.")) {
            return FabricLoader.getInstance().getModContainer("osweather").map(mod -> mod.getMetadata().getVersion().getFriendlyString().equals("1.1")).orElse(false);
        }
        if (mixinClassName.startsWith("com.ludwici.cobblewatchcore.mixin.rustlingspots.")) {
            return FabricLoader.getInstance().getModContainer("rustlingspots").map(mod -> mod.getMetadata().getVersion().getFriendlyString().equals("6.0")).orElse(false);
        }
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
