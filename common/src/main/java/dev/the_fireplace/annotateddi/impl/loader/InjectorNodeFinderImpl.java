package dev.the_fireplace.annotateddi.impl.loader;

import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import dev.the_fireplace.annotateddi.api.di.Implementation;
import dev.the_fireplace.annotateddi.impl.datastructure.LargePowerSet;
import dev.the_fireplace.annotateddi.impl.domain.loader.InjectorNodeFinder;
import dev.the_fireplace.annotateddi.impl.domain.loader.LoaderHelper;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
@Implementation
public final class InjectorNodeFinderImpl implements InjectorNodeFinder
{
    private static final String ROOT_MOD_ID = "minecraft";
    private final LoaderHelper loaderHelper;
    private final Set<String> loadedMods;
    private final Map<String, Set<String>> childMods;
    private final Map<String, Set<String>> parentMods;
    private final Set<Node> loadedNodes;
    private final Map<String, Node> nodesByModId;
    private final Map<Node, Set<Node>> childNodes;
    private final Map<Node, Set<Node>> parentNodes;
    private final Map<Node, Set<Node>> dependencyTree;
    private final Map<String, Node> childParentNodes;

    @Inject
    public InjectorNodeFinderImpl(LoaderHelper loaderHelper) {
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
        Node rootNode = new Node(Set.of(ROOT_MOD_ID));
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
            Node node = new Node(codependencies);
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
        for (Map.Entry<Node, Set<Node>> entry : dependencyTree.entrySet()) {
            Node parents = entry.getKey();
            Collection<Node> children = entry.getValue();
            for (Node childNode : children) {
                for (String child : childNode.getModIds()) {
                    childParentNodes.put(child, parents);
                }
            }
        }
    }

    private void populateDependencyTree(Node parentNode, Set<Node> parentDependencies, Collection<Node> children) {
        parentDependencies = new HashSet<>(parentDependencies);
        parentDependencies.add(parentNode);
        Set<Node> nodes = new HashSet<>();

        Set<Node> remainingChildren = new HashSet<>();
        for (Node evaluatingChildNode : children) {
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

    private Collection<Node> getNodesStartingCombinedBranches(Set<Node> parentDependencies, Set<Node> remainingChildren) {
        Collection<Node> nodes = new HashSet<>();
        Set<Node> candidateCombinedBranchStarts = getCandidateCombinedBranchStarts(parentDependencies, remainingChildren);
        for (int groupSize = 2; groupSize <= candidateCombinedBranchStarts.size(); groupSize++) {
            LargePowerSet<Node> powerSet = new LargePowerSet<>(candidateCombinedBranchStarts, groupSize, groupSize);
            boolean finishedScan = false;
            do {
                Collection<Node> confirmedCandidates = new HashSet<>();
                boolean rebuildPowerSet = false;
                for (LargePowerSet.LargeIterator<Node> iterator = powerSet.iterator(); iterator.hasNext(); ) {
                    Set<Node> candidateGroup = iterator.next();
                    if (candidateGroup.stream().anyMatch(confirmedCandidates::contains)) {
                        continue;
                    }
                    boolean startsCombinedBranch = nodeStartsCombinedBranch(parentDependencies, candidateGroup);
                    if (startsCombinedBranch) {
                        nodes.add(candidateGroup.stream().findAny().orElseThrow());
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

    private boolean nodeStartsCombinedBranch(Set<Node> parentDependencies, Set<Node> candidateGroup) {
        Node[] remainingCandidatesInGroup = candidateGroup.toArray(new Node[0]);
        Node firstCandidate = remainingCandidatesInGroup[0];
        remainingCandidatesInGroup = Arrays.copyOfRange(remainingCandidatesInGroup, 1, remainingCandidatesInGroup.length);

        Collection<Node> evaluatingGroupAllChildren = new HashSet<>(Set.of(remainingCandidatesInGroup));
        for (Node candidate : candidateGroup) {
            evaluatingGroupAllChildren.addAll(getAllChildren(candidate));
        }
        boolean isSelfContainedGrouping = this.isSelfContainedBranch(parentDependencies, firstCandidate, evaluatingGroupAllChildren);
        if (isSelfContainedGrouping) {
            populateDependencyTree(firstCandidate, parentDependencies, evaluatingGroupAllChildren);
            return true;
        }

        return false;
    }

    private Set<Node> getAllChildren(Node node) {
        return new HashSet<>(childNodes.getOrDefault(node, Collections.emptySet()));
    }

    private Set<Node> getAllDependencies(Node node) {
        return new HashSet<>(parentNodes.getOrDefault(node, Collections.emptySet()));
    }

    private boolean nodeStartsSelfContainedBranch(Set<Node> parentDependencies, Node evaluatingNode) {
        Collection<Node> evaluatingModAllChildren = getAllChildren(evaluatingNode);
        boolean hasNoChildren = evaluatingModAllChildren.isEmpty();
        Collection<Node> evaluatingModDependencies = getAllDependencies(evaluatingNode);
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

    private boolean isSelfContainedBranch(Collection<Node> parentDependencies, Node evaluatingNode, Collection<Node> evaluatingNodeAllChildren) {
        Collection<Node> nodeDependencies = new HashSet<>();
        for (Node nodeChild : evaluatingNodeAllChildren) {
            nodeDependencies.addAll(getAllDependencies(nodeChild));
        }
        Collection<Node> branchSelfContainedDependencies = new HashSet<>(parentDependencies);
        branchSelfContainedDependencies.addAll(evaluatingNodeAllChildren);
        branchSelfContainedDependencies.add(evaluatingNode);

        return branchSelfContainedDependencies.containsAll(nodeDependencies);
    }

    private Set<Node> getCandidateCombinedBranchStarts(Set<Node> parentDependencies, Set<Node> remainingChildren) {
        Set<Node> candidateLeaves = new HashSet<>();
        for (Node evaluatingChildNode : remainingChildren) {
            Set<Node> evaluatingChildDependencies = getAllDependencies(evaluatingChildNode);
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
        for (Node node : loadedNodes) {
            getParents(node);
        }
    }

    private Collection<Node> getParents(Node node) {
        if (parentNodes.containsKey(node)) {
            return parentNodes.get(node);
        }
        Collection<Node> parents = this.parentNodes.computeIfAbsent(node, k -> new HashSet<>());

        for (String modId : node.getModIds()) {
            for (String parentModId : getParents(modId)) {
                Node parentNode = this.nodesByModId.get(parentModId);
                if (!parentNode.equals(node)) {
                    parents.add(parentNode);
                }
            }
        }

        return this.parentNodes.get(node);
    }

    private void populateAllChildNodes() {
        for (Node node : loadedNodes) {
            for (Node parent : getParents(node)) {
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

    @Override
    @Nullable
    public Collection<String> getParentNode(String modId) {
        Node parentNode = childParentNodes.get(modId);

        return parentNode == null ? null : parentNode.getModIds();
    }

    @Override
    public Collection<String> getNode(String modId) {
        if (this.nodesByModId.containsKey(modId)) {
            return this.nodesByModId.get(modId).getModIds();
        }
        return Set.of(modId);
    }

    private static class Node
    {
        private final Set<String> modIds;

        private Node(Set<String> modIds) {
            this.modIds = modIds;
        }

        public Set<String> getModIds() {
            return modIds;
        }

        public Node with(String modId) {
            return withAll(Set.of(modId));
        }

        public Node withAll(Set<String> modIds) {
            return new Node(Sets.union(this.modIds, modIds));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Node otherNode) {
                return this.modIds.equals(otherNode.modIds);
            }
            return false;
        }
    }
}
