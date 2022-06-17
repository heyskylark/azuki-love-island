import BracketGroup from "./BracketGroup";

interface GenderedVoteBracket {
    seasonNumber: number;
    bracketNumber: number;
    twitterHandle: string;
    maleBracketGroups: BracketGroup[];
    femaleBracketGroups: BracketGroup[];
    finishedVoting: boolean;
}

export default GenderedVoteBracket;
