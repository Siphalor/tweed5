package de.siphalor.tweed5.core.api.middleware;

import de.siphalor.tweed5.core.api.sort.AcyclicGraphSorter;
import lombok.Getter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultMiddlewareContainer<M> implements MiddlewareContainer<M> {
	private static final String CONTAINER_ID = "";

	@Getter
	private List<Middleware<M>> middlewares = new ArrayList<>();
	private final Set<String> middlewareIds = new HashSet<>();
	private boolean sealed = false;

	@Override
	public String id() {
		return CONTAINER_ID;
	}

	@Override
	public void registerAll(Collection<Middleware<M>> middlewares) {
		requireUnsealed();

		for (Middleware<M> middleware : middlewares) {
			if (middleware.id().isEmpty()) {
				throw new IllegalArgumentException("Middleware id cannot be empty");
			}
			if (!this.middlewareIds.add(middleware.id())) {
				throw new IllegalArgumentException("Middleware id already registered: " + middleware.id());
			}
		}
		this.middlewares.addAll(middlewares);
	}

	@Override
	public void register(Middleware<M> middleware) {
		requireUnsealed();

		if (middleware.id().isEmpty()) {
			throw new IllegalArgumentException("Middleware id cannot be empty");
		}
		if (!middlewareIds.add(middleware.id())) {
			throw new IllegalArgumentException("Middleware id already registered: " + middleware.id());
		}
		middlewares.add(middleware);
	}

	private void requireUnsealed() {
		if (sealed) {
			throw new IllegalStateException("Middleware container has already been sealed");
		}
	}

	@Override
	public void seal() {
		if (sealed) {
			return;
		}

		sealed = true;

		String[] allMentionedMiddlewareIds = middlewares.stream()
				.flatMap(middleware -> Stream.concat(
						Stream.of(middleware.id()),
						Stream.concat(middleware.mustComeAfter().stream(), middleware.mustComeBefore().stream())
				)).distinct().toArray(String[]::new);

		Map<String, Integer> indecesByMiddlewareId = new HashMap<>();
		for (int i = 0; i < allMentionedMiddlewareIds.length; i++) {
			indecesByMiddlewareId.put(allMentionedMiddlewareIds[i], i);
		}

		AcyclicGraphSorter sorter = new AcyclicGraphSorter(allMentionedMiddlewareIds.length);

		for (Middleware<M> middleware : middlewares) {
			Integer currentIndex = indecesByMiddlewareId.get(middleware.id());

			middleware.mustComeAfter().stream()
					.map(indecesByMiddlewareId::get)
					.forEach(beforeIndex -> sorter.addEdge(beforeIndex, currentIndex));
			middleware.mustComeBefore().stream()
					.map(indecesByMiddlewareId::get)
					.forEach(afterIndex -> sorter.addEdge(currentIndex, afterIndex));
		}

		Map<String, Middleware<M>> middlewaresById = middlewares.stream().collect(Collectors.toMap(Middleware::id, Function.identity()));

		try {
			int[] sortedIndeces = sorter.sort();

			middlewares = Arrays.stream(sortedIndeces)
					.mapToObj(index -> allMentionedMiddlewareIds[index])
					.map(middlewaresById::get)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		} catch (AcyclicGraphSorter.GraphCycleException e) {
			StringBuilder messageBuilder = new StringBuilder("Found cycle in middleware dependencies: ");
			e.cycleIndeces().forEach(index -> messageBuilder.append(allMentionedMiddlewareIds[index]).append(" -> "));
			messageBuilder.append(allMentionedMiddlewareIds[e.cycleIndeces().iterator().next()]);

			throw new IllegalStateException(messageBuilder.toString(), e);
		}
	}

	@Override
	public M process(M inner) {
		if (!sealed) {
			throw new IllegalStateException("Middleware container has not been sealed");
		}
		M combined = inner;
		for (int i = middlewares.size() - 1; i >= 0; i--) {
			Middleware<M> middleware = middlewares.get(i);
			combined = middleware.process(combined);
		}
		return combined;
	}
}
