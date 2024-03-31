/**
 * Copyright (c) 2023,2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package ee.jakarta.tck.data.framework.read.only;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import ee.jakarta.tck.data.framework.read.only.NaturalNumber.NumberType;
import jakarta.data.Limit;
import jakarta.data.Order;
import jakarta.data.page.CursoredPage;
import jakarta.data.page.Page;
import jakarta.data.page.PageRequest;
import jakarta.data.repository.Find;
import jakarta.data.repository.OrderBy;
import jakarta.data.repository.Param;
import jakarta.data.repository.Query;
import jakarta.data.repository.BasicRepository;
import jakarta.data.repository.Repository;

/**
 * This is a read only repository that shares the same data (and entity type)
 * as the NaturalNumbers repository: the positive integers 1-100.
 * This repository is pre-populated at test startup and verified prior to running tests.
 */
@Repository
public interface PositiveIntegers extends BasicRepository<NaturalNumber, Long> {
    @Query("select count(this) where id(this) < ?1")
    long countByIdLessThan(long number);

    @Query("select count(this)>0 where id > ?1")
    boolean existsByIdGreaterThan(Long number);

    @Query("where floorOfSquareRoot <> ?1 and id < ?2")
    @OrderBy("numBitsRequired")
    CursoredPage<NaturalNumber> findByFloorOfSquareRootNotAndIdLessThanOrderByNumBitsRequiredDesc(long excludeSqrt,
                                                                                                  long eclusiveMax,
                                                                                                  PageRequest<NaturalNumber> pagination);
    @Query("where isOdd=true and id <= ?1")
    @OrderBy(value = "id", descending = true)
    List<NaturalNumber> findByIsOddTrueAndIdLessThanEqualOrderByIdDesc(long max);

    @Query("where isOdd=false and id between ?1 and ?2")
    List<NaturalNumber> findByIsOddFalseAndIdBetween(long min, long max);

    @Query("where numType in ?1 order by id asc")
    Stream<NaturalNumber> findByNumTypeInOrderByIdAsc(List<NumberType> types, Limit limit);

    @Query("where numType = ?1 or floorOfSquareRoot = ?2")
    Stream<NaturalNumber> findByNumTypeOrFloorOfSquareRoot(NumberType type, long floor);

    @Find
    Page<NaturalNumber> findMatching(long floorOfSquareRoot, Short numBitsRequired, NumberType numType,
            PageRequest<NaturalNumber> pagination);

    @Find
    Optional<NaturalNumber> findNumber(long id);

    @Find
    List<NaturalNumber> findOdd(boolean isOdd, NumberType numType, Limit limit, Order<NaturalNumber> sorts);

    @Query("Select id Where isOdd = true and (id = :id or id < :exclusiveMax) Order by id Desc")
    List<Long> oddAndEqualToOrBelow(long id, long exclusiveMax);

    // Per the spec: The 'and' operator has higher precedence than 'or'.
    @Query("WHERE numBitsRequired = :bits OR numType = :type AND id < :xmax")
    CursoredPage<NaturalNumber> withBitCountOrOfTypeAndBelow(@Param("bits") short bitsRequired,
                                                             @Param("type") NumberType numberType,
                                                             @Param("xmax") long exclusiveMax,
                                                             PageRequest<?> pageRequest);
}