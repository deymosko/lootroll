package org.deymosko.lootroll;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Matches registry entries against a config list of IDs ("minecraft:warden"),
 * tags ("#forge:bosses") and whole namespaces ("alexscaves:*").
 * The parsed form is cached until the config list changes.
 */
public class RegistryMatcher<T> {
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final Supplier<List<? extends String>> source;

    private List<? extends String> parsedFrom;
    private Set<ResourceLocation> ids = Set.of();
    private Set<String> namespaces = Set.of();
    private List<TagKey<T>> tags = List.of();

    public RegistryMatcher(ResourceKey<? extends Registry<T>> registryKey, Supplier<List<? extends String>> source) {
        this.registryKey = registryKey;
        this.source = source;
    }

    public boolean matches(Holder<T> holder) {
        refresh();
        if (ids.isEmpty() && namespaces.isEmpty() && tags.isEmpty()) return false;

        ResourceLocation id = holder.unwrapKey().map(ResourceKey::location).orElse(null);
        if (id != null && (ids.contains(id) || namespaces.contains(id.getNamespace()))) return true;
        for (TagKey<T> tag : tags) {
            if (holder.is(tag)) return true;
        }
        return false;
    }

    private void refresh() {
        List<? extends String> current = source.get();
        if (current == parsedFrom) return;

        Set<ResourceLocation> newIds = new HashSet<>();
        Set<String> newNamespaces = new HashSet<>();
        List<TagKey<T>> newTags = new ArrayList<>();
        for (String raw : current) {
            String entry = raw.trim();
            if (entry.startsWith("#")) {
                ResourceLocation tag = ResourceLocation.tryParse(entry.substring(1));
                if (tag != null) newTags.add(TagKey.create(registryKey, tag));
                else Lootroll.LOGGER.warn("Ignoring invalid tag '{}' in LootRoll config", entry);
            } else if (entry.endsWith(":*")) {
                newNamespaces.add(entry.substring(0, entry.length() - 2));
            } else {
                ResourceLocation id = ResourceLocation.tryParse(entry);
                if (id != null) newIds.add(id);
                else Lootroll.LOGGER.warn("Ignoring invalid id '{}' in LootRoll config", entry);
            }
        }
        ids = newIds;
        namespaces = newNamespaces;
        tags = newTags;
        parsedFrom = current;
    }
}
