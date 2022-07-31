interface TweetAttachments {
    mediaKeys: string[];
}

interface IslandTweets {
    id: string;
    text: string;
    createdAt: number;
    attachments?: TweetAttachments
}

export default IslandTweets;
