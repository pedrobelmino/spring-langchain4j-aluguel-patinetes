package br.com.pedrobelmino.langchain4j.hello.repository;

import br.com.pedrobelmino.langchain4j.hello.model.RentalData;
import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RentalRepository extends FirestoreReactiveRepository<RentalData> {
}
