interface GenderedRoundWinners {
    maleWinners: WinningResultsBracketGroup[];
    femaleWinners: WinningResultsBracketGroup[];
    roundNumber: number;
}

export interface WinningResultsBracketGroup {
    submissionId1: string;
    submissionId2: string;
    sortOrder: number;
}

export default GenderedRoundWinners;
