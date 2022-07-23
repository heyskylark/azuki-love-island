import BracketGroup from "./BracketGroup";

interface GenderedInitialBracket {
    seasonNumber: number;
    voteStartDate: number;
    voteDeadline: number;
    voteGapTimeMilliseconds?: number;
    numOfBrackets: number;
    maleBracketGroups: BracketGroup[];
    femaleBracketGroups: BracketGroup[];
}

export default GenderedInitialBracket;
