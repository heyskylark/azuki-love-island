interface Props {
    footerType: number
}

function Footer(props: Props) {
    function beanzFooter(): JSX.Element {
        return (
            <footer className="flex lg:container lg:mx-auto lg:py-6 underline text-sm text-gray-400 max-w-11xl mx-auto lg:px-8">
                <a className="lg:block hidden mr-4" href="https://twitter.com/heyskylark" target="_blank" rel="noreferrer">@heyskylark.</a>
                <a className="lg:block hidden" href="https://twitter.com/aetherfloweth" target="_blank" rel="noreferrer">@aetherfloweth.</a>
                <div className="w-1/2 block lg:hidden" />
                <img className="block w-1/2 right-0 z-10 block lg:hidden" src="images/sexy-beanz-cropped.png" alt="Love Island Beanz" />
            </footer>
        );
    }

    function regularFooter(): JSX.Element {
        return (
            <footer className="flex container mx-auto py-6 underline text-sm text-gray-400 max-w-11xl px-8">
                <a className="mr-4" href="https://twitter.com/heyskylark" target="_blank" rel="noreferrer">@heyskylark.</a>
                <a href="https://twitter.com/aetherfloweth" target="_blank" rel="noreferrer">@aetherfloweth.</a>
            </footer>
        );
    }

    function renderFooter(): JSX.Element {
        if (props.footerType === 1) {
            return beanzFooter();
        } else {
            return regularFooter();
        }
    }

    return renderFooter();
}

export default Footer;
