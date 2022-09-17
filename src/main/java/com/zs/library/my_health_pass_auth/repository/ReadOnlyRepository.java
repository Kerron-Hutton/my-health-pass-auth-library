package com.zs.library.my_health_pass_auth.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

@NoRepositoryBean
interface ReadOnlyRepository<T, ID> extends Repository<T, ID> {

  /**
   * Retrieves table record by ID.
   *
   * @param id id to query database
   * @return table record if found
   */
  Optional<T> findById(ID id);

  /**
   * Retrieves all records from table.
   *
   * @return all records from table
   */
  List<T> findAll();

}
