package dev.the_fireplace.annotateddi.impl.loader;

import com.google.common.collect.Sets;
import dev.the_fireplace.annotateddi.impl.datastructure.LargePowerSet;
import dev.the_fireplace.annotateddi.impl.domain.loader.LoaderHelper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public final class InjectorTreeBuilder
{
    private static final String ROOT_MOD_ID = "minecraft";
    private final LoaderHelper loaderHelper;
    private final Set<String> loadedMods;
    private final Map<String, Set<String>> childMods;
    private final Map<String, Set<String>> parentMods;
    private final Set<InjectorNode> loadedNodes;
    private final Map<String, InjectorNode> nodesByModId;
    private final Map<InjectorNode, Set<InjectorNode>> childNodes;
    private final Map<InjectorNode, Set<InjectorNode>> parentNodes;
    private final Map<InjectorNode, Set<InjectorNode>> dependencyTree;
    private final Map<String, InjectorNode> childParentNodes;

    @Inject
    public InjectorTreeBuilder(LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
        loadedMods = loaderHelper.getLoadedMods().stream()
            .filter(modId -> loaderHelper.findDiConfigPath(modId).isPresent())
            .collect(Collectors.toSet());
        childMods = new HashMap<>(5);
        parentMods = new HashMap<>(5);
        loadedNodes = new HashSet<>(5);
        nodesByModId = new HashMap<>(5);
        childNodes = new HashMap<>(5);
        parentNodes = new HashMap<>(5);
        dependencyTree = new HashMap<>(5);
        childParentNodes = new HashMap<>(5);
        buildTree();
    }

    private void buildTree() {
        populateAllParentMods();
        populateAllChildMods();
        InjectorNode rootNode = new InjectorNode(Sets.newHashSet(ROOT_MOD_ID));
        if (this.loaderHelper.isModLoaded("java")) {
            rootNode = rootNode.with("java");
            nodesByModId.put("java", rootNode);
        }
        loadedNodes.add(rootNode);
        nodesByModId.put(ROOT_MOD_ID, rootNode);
        populateLoadedNodes();
        childMods.clear();
        populateAllParentNodes();
        parentMods.clear();
        populateAllChildNodes();
        populateDependencyTree(rootNode, new HashSet<>(), getAllChildren(rootNode));
        parentNodes.clear();
        childNodes.clear();
        calculateImmediateParents();
        dependencyTree.clear();
    }

    private void populateLoadedNodes() {
        for (String modId : loadedMods) {
            if (this.nodesByModId.containsKey(modId)) {
                continue;
            }
            Set<String> codependencies = getCodependencies(modId, new HashSet<>());
            InjectorNode node = new InjectorNode(codependencies);
            this.loadedNodes.add(node);
            for (String nodeMod : node.getModIds()) {
                this.nodesByModId.put(nodeMod, node);
            }
        }
    }

    private Set<String> getCodependencies(String modId, Set<String> visitedMods) {
        Set<String> codependencies = new HashSet<>(Sets.intersection(
            childMods.getOrDefault(modId, Collections.emptySet()),
            parentMods.getOrDefault(modId, Collections.emptySet())
        ));
        codependencies.add(modId);
        visitedMods.add(modId);
        for (String codependency : codependencies) {
            if (!visitedMods.contains(codependency)) {
                codependencies.addAll(getCodependencies(codependency, visitedMods));
            }
        }
        return codependencies;
    }

    private void calculateImmediateParents() {
        for (Map.Entry<InjectorNode, Set<InjectorNode>> entry : dependencyTree.entrySet()) {
            InjectorNode parents = entry.getKey();
            Collection<InjectorNode> children = entry.getValue();
            for (InjectorNode childNode : children) {
                for (String child : childNode.getModIds()) {
                    childParentNodes.put(child, parents);
                }
            }
        }
    }

    private void populateDependencyTree(InjectorNode parentNode, Set<InjectorNode> parentDependencies, Collection<InjectorNode> children) {
        parentDependencies = new HashSet<>(parentDependencies);
        parentDependencies.add(parentNode);
        Set<InjectorNode> nodes = new HashSet<>();

        Set<InjectorNode> remainingChildren = new HashSet<>();
        for (InjectorNode evaluatingChildNode : children) {
            boolean nodeStartsSelfContainedBranch = nodeStartsSelfContainedBranch(parentDependencies, evaluatingChildNode);
            if (nodeStartsSelfContainedBranch) {
                nodes.add(evaluatingChildNode);
            } else {
                remainingChildren.add(evaluatingChildNode);
            }
        }
        nodes.addAll(getNodesStartingCombinedBranches(parentDependencies, remainingChildren));

        dependencyTree.put(parentNode, nodes);
    }

    private Collection<InjectorNode> getNodesStartingCombinedBranches(Set<InjectorNode> parentDependencies, Set<InjectorNode> remainingChildren) {
        Collection<InjectorNode> nodes = new HashSet<>();
        Set<InjectorNode> candidateCombinedBranchStarts = getCandidateCombinedBranchStarts(parentDependencies, remainingChildren);
        for (int groupSize = 2; groupSize <= candidateCombinedBranchStarts.size(); groupSize++) {
            LargePowerSet<InjectorNode> powerSet = new LargePowerSet<>(candidateCombinedBranchStarts, groupSize, groupSize);
            boolean finishedScan = false;
            do {
                Collection<InjectorNode> confirmedCandidates = new HashSet<>();
                boolean rebuildPowerSet = false;
                for (LargePowerSet.LargeIterator<InjectorNode> iterator = powerSet.iterator(); iterator.hasNext(); ) {
                    Set<InjectorNode> candidateGroup = iterator.next();
                    if (candidateGroup.stream().anyMatch(confirmedCandidates::contains)) {
                        continue;
                    }
                    boolean startsCombinedBranch = nodeStartsCombinedBranch(parentDependencies, candidateGroup);
                    if (startsCombinedBranch) {
                        nodes.add(candidateGroup.stream().findAny().get());
                        candidateCombinedBranchStarts.removeAll(candidateGroup);
                        confirmedCandidates.addAll(candidateGroup);

                        if (candidateCombinedBranchStarts.size() < groupSize) {
                            break;
                        }

                        if (iterator.shouldBeRebuiltForPerformance(candidateCombinedBranchStarts.size())) {
                            rebuildPowerSet = true;
                            break;
                        }
                    }
                }
                if (rebuildPowerSet) {
                    confirmedCandidates.clear();
                    powerSet = new LargePowerSet<>(candidateCombinedBranchStarts, groupSize, groupSize);
                } else {
                    finishedScan = true;
                }
            } while (!finishedScan);
        }

        return nodes;
    }

    private boolean nodeStartsCombinedBranch(Set<InjectorNode> parentDependencies, Set<InjectorNode> candidateGroup) {
        InjectorNode[] remainingCandidatesInGroup = candidateGroup.toArray(new InjectorNode[0]);
        InjectorNode firstCandidate = remainingCandidatesInGroup[0];
        remainingCandidatesInGroup = Arrays.copyOfRange(remainingCandidatesInGroup, 1, remainingCandidatesInGroup.length);

        Collection<InjectorNode> evaluatingGroupAllChildren = new HashSet<>(Sets.newHashSet(remainingCandidatesInGroup));
        for (InjectorNode candidate : candidateGroup) {
            evaluatingGroupAllChildren.addAll(getAllChildren(candidate));
        }
        boolean isSelfContainedGrouping = this.isSelfContainedBranch(parentDependencies, firstCandidate, evaluatingGroupAllChildren);
        if (isSelfContainedGrouping) {
            populateDependencyTree(firstCandidate, parentDependencies, evaluatingGroupAllChildren);
            return true;
        }

        return false;
    }

    private Set<InjectorNode> getAllChildren(InjectorNode node) {
        return new HashSet<>(childNodes.getOrDefault(node, Collections.emptySet()));
    }

    private Set<InjectorNode> getAllDependencies(InjectorNode node) {
        return new HashSet<>(parentNodes.getOrDefault(node, Collections.emptySet()));
    }

    private boolean nodeStartsSelfContainedBranch(Set<InjectorNode> parentDependencies, InjectorNode evaluatingNode) {
        Collection<InjectorNode> evaluatingModAllChildren = getAllChildren(evaluatingNode);
        boolean hasNoChildren = evaluatingModAllChildren.isEmpty();
        Collection<InjectorNode> evaluatingModDependencies = getAllDependencies(evaluatingNode);
        boolean branchMeetsImmediateDependencies = parentDependencies.containsAll(evaluatingModDependencies);
        if (branchMeetsImmediateDependencies) {
            if (hasNoChildren) {
                dependencyTree.put(evaluatingNode, Collections.emptySet());
                return true;
            }
            boolean isSelfContainedBranch = isSelfContainedBranch(parentDependencies, evaluatingNode, evaluatingModAllChildren);
            if (isSelfContainedBranch) {
                populateDependencyTree(evaluatingNode, parentDependencies, evaluatingModAllChildren);
                return true;
            }
        }

        return false;
    }

    private boolean isSelfContainedBranch(Collection<InjectorNode> parentDependencies, InjectorNode evaluatingNode, Collection<InjectorNode> evaluatingNodeAllChildren) {
        Collection<InjectorNode> nodeDependencies = new HashSet<>();
        for (InjectorNode nodeChild : evaluatingNodeAllChildren) {
            nodeDependencies.addAll(getAllDependencies(nodeChild));
        }
        Collection<InjectorNode> branchSelfContainedDependencies = new HashSet<>(parentDependencies);
        branchSelfContainedDependencies.addAll(evaluatingNodeAllChildren);
        branchSelfContainedDependencies.add(evaluatingNode);

        return branchSelfContainedDependencies.containsAll(nodeDependencies);
    }

    private Set<InjectorNode> getCandidateCombinedBranchStarts(Set<InjectorNode> parentDependencies, Set<InjectorNode> remainingChildren) {
        Set<InjectorNode> candidateLeaves = new HashSet<>();
        for (InjectorNode evaluatingChildNode : remainingChildren) {
            Set<InjectorNode> evaluatingChildDependencies = getAllDependencies(evaluatingChildNode);
            boolean branchMeetsImmediateDependencies = parentDependencies.containsAll(evaluatingChildDependencies);
            if (branchMeetsImmediateDependencies) {
                candidateLeaves.add(evaluatingChildNode);
            }
        }

        return candidateLeaves;
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

    private void populateAllParentNodes() {
        for (InjectorNode node : loadedNodes) {
            getParents(node);
        }
    }

    private Collection<InjectorNode> getParents(InjectorNode node) {
        if (parentNodes.containsKey(node)) {
            return parentNodes.get(node);
        }
        Collection<InjectorNode> parents = this.parentNodes.computeIfAbsent(node, k -> new HashSet<>());

        for (String modId : node.getModIds()) {
            for (String parentModId : getParents(modId)) {
                InjectorNode parentNode = this.nodesByModId.get(parentModId);
                if (!parentNode.equals(node)) {
                    parents.add(parentNode);
                }
            }
        }

        return this.parentNodes.get(node);
    }

    private void populateAllChildNodes() {
        for (InjectorNode node : loadedNodes) {
            for (InjectorNode parent : getParents(node)) {
                this.childNodes.computeIfAbsent(parent, k -> new HashSet<>()).add(node);
            }
        }
    }

    private void populateAllChildMods() {
        for (String modId : loadedMods) {
            for (String parent : getParents(modId)) {
                this.childMods.computeIfAbsent(parent, k -> new HashSet<>()).add(modId);
            }
        }
    }

    public Map<String, InjectorNode> getNodesByModId() {
        return nodesByModId;
    }

    public Map<String, InjectorNode> getChildParentNodes() {
        return childParentNodes;
    }
}
