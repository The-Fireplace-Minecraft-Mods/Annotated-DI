package dev.the_fireplace.annotateddi.impl.loader;

import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.annotateddi.impl.domain.loader.InjectorNodeFinder;
import dev.the_fireplace.annotateddi.impl.domain.loader.LoaderHelper;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

@Singleton
@Implementation
public final class InjectorNodeFinderImpl implements InjectorNodeFinder
{
    private static final String ROOT_MOD_ID = "minecraft";
    private final LoaderHelper loaderHelper;
    private final Map<String, Collection<String>> childMods;
    private final Map<String, Collection<String>> parentMods;
    private final Map<Collection<String>, Collection<Collection<String>>> dependencyTree;
    private final Map<String, Collection<String>> childParentNodes;
    private final Collection<String> loadedMods;

    @Inject
    public InjectorNodeFinderImpl(LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
        childMods = new HashMap<>(5);
        parentMods = new HashMap<>(5);
        dependencyTree = new HashMap<>(5);
        childParentNodes = new HashMap<>(5);
        loadedMods = loaderHelper.getLoadedMods();
        buildTree();
    }

    private void buildTree() {
        populateAllParentMods();
        populateAllChildMods();
        Collection<String> rootNode = Sets.newHashSet(ROOT_MOD_ID);
        if (this.loaderHelper.isModLoaded("java")) {
            rootNode.add("java");
        }
        populateDependencyTree(rootNode, new HashSet<>());
        calculateImmediateParents();
    }

    private void calculateImmediateParents() {
        for (Map.Entry<Collection<String>, Collection<Collection<String>>> entry : dependencyTree.entrySet()) {
            Collection<String> parents = entry.getKey();
            Collection<Collection<String>> children = entry.getValue();
            for (Collection<String> childNode : children) {
                for (String child : childNode) {
                    childParentNodes.put(child, parents);
                }
            }
        }
    }

    private void populateDependencyTree(Collection<String> parentNode, Collection<String> parentDependencies) {
        boolean isPopulatingCodependentMod = parentDependencies.containsAll(parentNode);
        if (isPopulatingCodependentMod) {
            return;
        }
        parentDependencies = new HashSet<>(parentDependencies);
        parentDependencies.addAll(parentNode);
        Collection<String> possibleChildren = new HashSet<>();
        for (String mod : parentNode) {
            possibleChildren.addAll(childMods.getOrDefault(mod, Collections.emptySet()));
        }
        Collection<Collection<String>> nodes = new HashSet<>();

        for (String currentMod : possibleChildren) {
            getNodeIfAttachable(parentNode, parentDependencies, currentMod)
                .ifPresent(nodes::add);
        }

        dependencyTree.put(parentNode, nodes);
    }

    private Optional<Collection<String>> getNodeIfAttachable(Collection<String> parentNode, Collection<String> parentDependencies, String evaluatingModId) {
        if (parentNode.contains(evaluatingModId)) {
            return Optional.empty();
        }
        Collection<String> evaluatingModAllChildren = childMods.getOrDefault(evaluatingModId, Collections.emptySet());
        boolean hasNoChildren = evaluatingModAllChildren.isEmpty();
        Collection<String> evaluatingModDependencies = parentMods.getOrDefault(evaluatingModId, Collections.emptySet());
        boolean branchMeetsImmediateDependencies = parentDependencies.containsAll(evaluatingModDependencies);
        if (branchMeetsImmediateDependencies) {
            Set<String> node = Sets.newHashSet(evaluatingModId);
            if (hasNoChildren) {
                dependencyTree.put(node, Collections.emptySet());
                return Optional.of(node);
            }
            boolean isSelfContainedBranch = isSelfContainedBranch(parentDependencies, evaluatingModId, evaluatingModAllChildren);
            if (isSelfContainedBranch) {
                populateDependencyTree(node, parentDependencies);
                return Optional.of(node);
            }
            return Optional.empty();
        }
        if (hasNoChildren) {
            return Optional.empty();
        }
        Collection<String> unfulfilledDependencies = new HashSet<>(evaluatingModDependencies);
        unfulfilledDependencies.removeAll(parentDependencies);
        boolean isCodependent = evaluatingModAllChildren.containsAll(unfulfilledDependencies);
        if (isCodependent) {
            Set<String> node = new HashSet<>(unfulfilledDependencies);
            node.add(evaluatingModId);

            boolean isSelfContainedBranch = isSelfContainedBranch(parentDependencies, evaluatingModId, evaluatingModAllChildren);
            if (isSelfContainedBranch) {
                populateDependencyTree(node, parentDependencies);
                return Optional.of(node);
            }
        }

        return Optional.empty();
    }

    private boolean isSelfContainedBranch(Collection<String> parentDependencies, String evaluatingMod, Collection<String> evaluatingModAllChildren) {
        Collection<String> nodeDependencies = new HashSet<>();
        for (String nodeChild : evaluatingModAllChildren) {
            nodeDependencies.addAll(parentMods.getOrDefault(nodeChild, Collections.emptySet()));
        }
        Collection<String> branchSelfContainedDependencies = new HashSet<>(parentDependencies);
        branchSelfContainedDependencies.addAll(evaluatingModAllChildren);
        branchSelfContainedDependencies.add(evaluatingMod);

        return branchSelfContainedDependencies.containsAll(nodeDependencies);
    }

    private void populateAllParentMods() {
        for (String modId : loadedMods) {
            getParents(modId);
        }
    }

    private Collection<String> getParents(String modId) {
        if (parentMods.containsKey(modId)) {
            return parentMods.get(modId);
        }
        Collection<String> parents = this.parentMods.computeIfAbsent(modId, k -> new HashSet<>());
        Collection<String> immediateParents = loaderHelper.getDependencies(modId);
        for (String parent : immediateParents) {
            if (loadedMods.contains(parent)) {
                parents.add(parent);
                parents.addAll(getParents(parent));
                parents.remove(modId);
            }
        }
        if (!this.parentMods.get(modId).contains(ROOT_MOD_ID) && !modId.equals(ROOT_MOD_ID)) {
            this.parentMods.get(modId).add(ROOT_MOD_ID);
        }

        return this.parentMods.get(modId);
    }

    private void populateAllChildMods() {
        for (String modId : loadedMods) {
            for (String parent : getParents(modId)) {
                this.childMods.computeIfAbsent(parent, k -> new HashSet<>()).add(modId);
            }
        }
    }

    @Override
    @Nullable
    public Collection<String> getParentNode(String modId) {
        Collection<String> parentNode = childParentNodes.get(modId);
        if (parentNode == null || parentNode.size() == 0) {
            return null;
        }
        return parentNode;
    }

    @Override
    public Collection<String> getNode(String modId) {
        for (Collection<String> node : dependencyTree.keySet()) {
            if (node.contains(modId)) {
                return node;
            }
        }

        return Sets.newHashSet(modId);
    }
}
