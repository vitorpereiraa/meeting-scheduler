package pj.domain.scheduleviva

import pj.domain.*
import pj.domain.DomainError.NoAvailableSlot
import pj.domain.SimpleTypes.*
import pj.domain.availability.{AvailabilityService, IntervalAlgebra}
import pj.domain.preference.PreferencesService

import scala.annotation.tailrec

/*
 * An edge exist between two nodes:
 * if they are not from the same viva,
 * if they share the same resources (viva.jury) and the availabilities do not overlap
 */
object ScheduleVivaServiceMS03Graph:

  final case class Node(viva: Viva, candidateAvailability: Availability, summedPreference: SummedPreference)
  final case class Edge(node: Node, preference: SummedPreference)

  def scheduleConflictingVivas(vivas: List[Viva], resources: List[Resource], duration: Duration): List[ScheduledViva] =
    val candidates: List[(Viva, List[(Availability, SummedPreference)])] =
      vivas.map(viva => (viva, findAllCandidateSchedules(viva, resources, duration)))

    val graph = constructGraph(candidates, resources, duration)
    val sortedNodes = topologicalSort(graph)
    longestPath(graph, sortedNodes)

  def constructGraph(candidates: List[(Viva, List[(Availability, SummedPreference)])], resources: List[Resource], duration: Duration): Map[Node, List[Edge]] =
    val graph: Map[Node, List[Edge]] = Map().withDefaultValue(List())

    // Create all nodes
    val nodes = for {
      (viva, availabilities) <- candidates
      (availability, preference) <- availabilities
    } yield Node(viva, availability, preference)

    // Create edges
    val edges = nodes.flatMap { nodeA =>
      nodes.collect {
        case nodeB if nodeA != nodeB && nodeA.viva != nodeB.viva && !conflicts(nodeA, nodeB) =>
          Edge(nodeB, nodeA.summedPreference + nodeB.summedPreference)
      }.map(edge => (nodeA, edge))
    }

    // Construct the graph
    edges.groupBy(_._1).map:
      case (node, edgeList) => node -> edgeList.map(_._2)

  def topologicalSort(graph: Map[Node, List[Edge]]): List[Node] =
    def dfs(node: Node, visited: Set[Node], stack: List[Node]): (Set[Node], List[Node]) =
      if (visited.contains(node)) (visited, stack)
      else
        val (newVisited, newStack) = graph(node).foldLeft((visited + node, stack)):
          case ((v, s), edge) =>
            val (nextVisited, nextStack) = dfs(edge.node, v, s)
            (nextVisited, nextStack)
        (newVisited, node :: newStack)

    val (visited, stack) = graph.keys.foldLeft((Set[Node](), List[Node]())):
      case ((v, s), node) => dfs(node, v, s)

    stack.reverse

  def longestPath(graph: Map[Node, List[Edge]], topoSortedNodes: List[Node]): List[ScheduledViva] =
    val distances = topoSortedNodes.foldLeft(Map[Node, Int]().withDefaultValue(Int.MinValue)) { (dist, node) =>
      val maxDist = graph(node).foldLeft(dist) { (d, edge) =>
        if (dist(node) + edge.preference.to > d(edge.node)) d.updated(edge.node, dist(node) + edge.preference.to) else d
      }
      maxDist.updated(node, dist.getOrElse(node, 0))
    }

    // Find the node with the maximum distance
    val (endNode, _) = distances.foldLeft((Option.empty[Node], Int.MinValue)):
      case ((maxNode, maxDist), (node, dist)) =>
        if (dist > maxDist) (Some(node), dist) else (maxNode, maxDist)

    def buildPath(node: Node, acc: List[ScheduledViva]): List[ScheduledViva] =
      val scheduledViva = ScheduledViva(node.viva.student, node.viva.title, node.viva.jury, node.candidateAvailability.start, node.candidateAvailability.end, node.summedPreference)
      val predecessors = graph.filter(_._2.exists(_.node == node)).keys

      if (predecessors.isEmpty) scheduledViva :: acc
      else
        val pred = predecessors.foldLeft((Option.empty[Node], Int.MinValue)) {
          case ((maxPred, maxDist), pred) =>
            if (distances(pred) > maxDist) (Some(pred), distances(pred)) else (maxPred, maxDist)
        }._1

        pred.fold(acc)(p => buildPath(p, scheduledViva :: acc))

    endNode.fold(List.empty[ScheduledViva])(node => buildPath(node, Nil))

  def findAllCandidateSchedules(viva: Viva, resources: List[Resource], duration: Duration): List[(Availability, SummedPreference)] =
   val availabilities = resources
      .filter(resource => viva.jury.exists(_.resource.id == resource.id))
      .map(_.availability)
      .map(availList =>
        availList
          .filter(a => a.end.isAfter(a.start.plus(duration)) || a.end.isEqual(a.start.plus(duration)))
      )
   AvailabilityService.intersectAll(availabilities, duration)

  def conflicts(nodeA: Node, nodeB: Node): Boolean =

    val resourceConflict = nodeA.viva.jury.intersect(nodeB.viva.jury).nonEmpty

    val timeConflict = IntervalAlgebra.intersectable(nodeA.candidateAvailability, nodeB.candidateAvailability)

    resourceConflict && timeConflict