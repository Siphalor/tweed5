package de.siphalor.tweed5.core.api.middleware;

import de.siphalor.tweed5.core.api.sort.AcyclicGraphSorter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultMiddlewareContainer<M> implements MiddlewareContainer<M> {
	private static final String CONTAINER_ID = "";

	private List<Middleware<M>> middlewares = new ArrayList<>();
	private final Set<String> middlewareIds = new HashSet<>();
	private boolean sealed = false;

	@Override
	public String id() {
		return CONTAINER_ID;
	}

	@Override
	public void register(Middleware<M> middleware) {
		if (sealed) {
			throw new IllegalStateException("Middleware container has already been sealed");
		}
		if (middleware.id().isEmpty()) {
			throw new IllegalArgumentException("Middleware id cannot be empty");
		}
		if (middlewareIds.contains(middleware.id())) {
			throw new IllegalArgumentException("Middleware id already registered: " + middleware.id());
		}

		middlewares.add(middleware);
		middlewareIds.add(middleware.id());
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
			throw new IllegalStateException(e);
		}
	}

	@Override
	public M process(M inner) {
		if (!sealed) {
			throw new IllegalStateException("Middleware container has not been sealed");
		}
		M combined = inner;
		for (Middleware<M> middleware : middlewares) {
			combined = middleware.process(combined);
		}
		return combined;
	}
}
