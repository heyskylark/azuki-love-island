import BracketGroup from "./BracketGroup";

interface GenderedVoteBracket {
    seasonNumber: number;
    roundNumber: number;
    maleBracketGroups: BracketGroup[];
    femaleBracketGroups: BracketGroup[];
    hasVoted: boolean;
    finishedVoting: boolean;
    twitterHandle?: string;
}

export default GenderedVoteBracket;
