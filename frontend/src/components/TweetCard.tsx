import { TwitterTweetEmbed, TwitterVideoEmbed } from "react-twitter-embed";
import IslandTweets from "../models/api/IslandTweetsResponse";
import Loading from "./Loading";

interface Props {
    tweet: IslandTweets
}

function TweetCard(props: Props) {
    return (
        <TwitterTweetEmbed
            tweetId={props.tweet.id} 
            placeholder={<Loading />}
            options={{
                hideThread: true
            }}
        />
    );
}

export default TweetCard;
