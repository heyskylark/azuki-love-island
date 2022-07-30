interface TweetAttachments {
    mediaKeys: string[];
}

interface IslandTweets {
    id: string;
    text: string;
    attachments?: TweetAttachments
}

export default IslandTweets;
