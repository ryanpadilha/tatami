package fr.ippon.tatami.repository;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import fr.ippon.tatami.domain.User;
import fr.ippon.tatami.service.util.RandomUtil;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Revision of Cassandra implementation of the RssUid repository for CCM (Cassandra Cluster Manager).
 * <p/>
 * Structure : - Key = "rss_uid" - Name = key - Value = email
 *
 * @author Pierre Rust
 * @author Derek Zuk
 */
@Repository
public class RssUidRepository {

    @Inject
    private Session session;

    private Mapper<User> mapper;

    private PreparedStatement generateRssUid;

    private PreparedStatement getUserEmailByRssUid;

    private PreparedStatement removeRssUid;

    @PostConstruct
    public void init() {
        mapper = new MappingManager(session).mapper(User.class);

        generateRssUid = session.prepare(
            "INSERT INTO rss (rss_uid,email) " +
                "VALUES (:rss_uid, :email)");

        getUserEmailByRssUid = session.prepare(
            "SELECT email FROM rss " +
                "WHERE rss_uid = :rss_uid");

        removeRssUid = session.prepare(
            "DELETE FROM rss " +
            "WHERE rss_uid = :rss_uid");

    }

    public String generateRssUid(String email) {
        BoundStatement stmt = generateRssUid.bind();
        String key = RandomUtil.generateActivationKey();
        stmt.setString("rss_uid", key);
        stmt.setString("email", email);
        session.execute(stmt);
        return key;
    }

    public String getUserEmailByRssUid(String rssUid) {
        BoundStatement stmt = getUserEmailByRssUid.bind();
        stmt.setString("rss_uid", rssUid);
        ResultSet results = session.execute(stmt);
        if (!results.isExhausted()) {
            return results.one().getString("email");
        }
        return null;
    }

    public void removeRssUid(String rssUid) {
        BoundStatement stmt = removeRssUid.bind();
        stmt.setString("rss_uid", rssUid);
        session.execute(stmt);
    }

}