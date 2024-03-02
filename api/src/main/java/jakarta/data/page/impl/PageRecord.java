/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 */
package jakarta.data.page.impl;

import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Record type implementing {@link Page}.
 * This may be used to simplify implementation of a repository interface.
 *
 * @param pageRequest The {@link PageRequest page request} for which this
 *                    slice was obtained
 * @param content The page content
 * @param totalElements The total number of elements across all pages that
 *                      can be requested for the query
 * @param moreResults whether there is a (nonempty) next page of results
 * @param <T> The type of elements on the page
 */
public record PageRecord<T>(PageRequest<T> pageRequest, List<T> content, long totalElements, boolean moreResults)
        implements Page<T> {

    public PageRecord(PageRequest<T> pageRequest, List<T> content, long totalElements) {
        this( pageRequest, content, totalElements,
                content.size() == pageRequest.size()
                        && totalElements > pageRequest.size() * pageRequest.page() );
    }

    @Override
    public boolean hasContent() {
        return !content.isEmpty();
    }

    @Override
    public int numberOfElements() {
        return content.size();
    }

    @Override
    public boolean hasNext() {
        return moreResults;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> PageRequest<E> pageRequest(Class<E> entityClass) {
        return (PageRequest<E>) pageRequest;
    }

    @Override
    public PageRequest<T> nextPageRequest() {
        if (moreResults) {
            return pageRequest.next();
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public boolean hasPrevious() {
        return pageRequest.page() > 1;
    }

    @Override
    public PageRequest<T> previousPageRequest() {
        return pageRequest.previous();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> PageRequest<E> previousPageRequest(Class<E> entityClass) {
        return (PageRequest<E>) previousPageRequest();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> PageRequest<E> nextPageRequest(Class<E> entityClass) {
        return (PageRequest<E>) nextPageRequest();
    }

    @Override
    public Iterator<T> iterator() {
        return content.iterator();
    }

    @Override
    public boolean hasTotals() {
        return totalElements >= 0;
    }

    @Override
    public long totalElements() {
        if (totalElements<0) {
            throw new IllegalStateException("total elements are not available");
        }
        return totalElements;
    }

    @Override
    public long totalPages() {
        if (totalElements<0) {
            throw new IllegalStateException("total elements are not available");
        }
        int size = pageRequest.size();
        return (totalElements + size - 1) / size;
    }
}
