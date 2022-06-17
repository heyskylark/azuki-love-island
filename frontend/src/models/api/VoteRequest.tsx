import BracketGroup from "./BracketGroup";

interface VoteRequest {
    twitterHandle: string;
    maleBracketGroups: BracketGroup[];
    femaleBracketGroups: BracketGroup[];
}

export default VoteRequest;
